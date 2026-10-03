#!/usr/bin/env python3
"""Audit native ELF and APK ZIP alignment for Android 16-KB page sizes.

Usage:
  python scripts/audit_16kb_artifact.py path/to/app-release.apk
  python scripts/audit_16kb_artifact.py path/to/app-release.aab

Google Play's 16-KB requirement is a 64-bit compatibility gate. Therefore
arm64-v8a and x86_64 native libraries are acceptance-gating. 32-bit ABI
problems are still reported as WARN so they remain visible without turning a
64-bit-ready artifact red solely because of legacy 32-bit binaries.

APK: verifies packaged .so ELF PT_LOAD/GNU_RELRO alignment and, for
uncompressed .so entries, the ZIP data offset is aligned to 16 KiB.
AAB: verifies ELF PT_LOAD/GNU_RELRO alignment. Final split-APK ZIP alignment
must also be verified with bundletool (`bundletool dump config ...` ->
PAGE_ALIGNMENT_16K) or on generated APKs.
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import struct
import sys
import zipfile

PAGE = 16 * 1024
PT_LOAD = 1
PT_GNU_RELRO = 0x6474E552
GATING_ABIS = {"arm64-v8a", "x86_64"}
NON_GATING_ABIS = {"armeabi-v7a", "x86"}


@dataclass
class ElfResult:
    ok: bool
    detail: str


def elf_16k(data: bytes) -> ElfResult:
    if len(data) < 64 or data[:4] != b'\x7fELF':
        return ElfResult(False, 'not an ELF file')
    elf_class = data[4]
    endian_id = data[5]
    if endian_id == 1:
        e = '<'
    elif endian_id == 2:
        e = '>'
    else:
        return ElfResult(False, f'unsupported ELF endian id {endian_id}')

    if elf_class == 1:  # ELF32
        if len(data) < 52:
            return ElfResult(False, 'truncated ELF32 header')
        phoff = struct.unpack_from(e + 'I', data, 28)[0]
        phentsize = struct.unpack_from(e + 'H', data, 42)[0]
        phnum = struct.unpack_from(e + 'H', data, 44)[0]
        align_off = 28
        offset_off = 4
        vaddr_off = 8
        memsz_off = 20
        width = 'I'
    elif elf_class == 2:  # ELF64
        if len(data) < 64:
            return ElfResult(False, 'truncated ELF64 header')
        phoff = struct.unpack_from(e + 'Q', data, 32)[0]
        phentsize = struct.unpack_from(e + 'H', data, 54)[0]
        phnum = struct.unpack_from(e + 'H', data, 56)[0]
        align_off = 48
        offset_off = 8
        vaddr_off = 16
        memsz_off = 40
        width = 'Q'
    else:
        return ElfResult(False, f'unsupported ELF class {elf_class}')

    load_count = 0
    bad_load = []
    aligns = []
    relro = []
    bad_relro = []
    for i in range(phnum):
        pos = phoff + i * phentsize
        if pos + phentsize > len(data) or phentsize < align_off + struct.calcsize(width):
            return ElfResult(False, 'truncated program-header table')
        p_type = struct.unpack_from(e + 'I', data, pos)[0]
        if p_type == PT_LOAD:
            load_count += 1
            p_offset = struct.unpack_from(e + width, data, pos + offset_off)[0]
            p_vaddr = struct.unpack_from(e + width, data, pos + vaddr_off)[0]
            p_align = struct.unpack_from(e + width, data, pos + align_off)[0]
            aligns.append(p_align)
            if p_align < PAGE or (p_offset - p_vaddr) % PAGE != 0:
                bad_load.append((i, p_offset, p_vaddr, p_align))
        elif p_type == PT_GNU_RELRO:
            p_vaddr = struct.unpack_from(e + width, data, pos + vaddr_off)[0]
            p_memsz = struct.unpack_from(e + width, data, pos + memsz_off)[0]
            relro.append((p_vaddr, p_memsz))
            if (p_vaddr + p_memsz) % PAGE != 0:
                bad_relro.append((i, p_vaddr, p_memsz))

    if load_count == 0:
        return ElfResult(False, 'no PT_LOAD segments found')
    problems = []
    if bad_load:
        problems.append('LOAD ' + '; '.join(
            f'ph#{i} off=0x{o:x} vaddr=0x{v:x} align=0x{a:x}' for i, o, v, a in bad_load
        ))
    if bad_relro:
        problems.append('RELRO ' + '; '.join(
            f'ph#{i} vaddr=0x{v:x} memsz=0x{m:x} endMod16K=0x{((v + m) % PAGE):x}'
            for i, v, m in bad_relro
        ))
    if problems:
        return ElfResult(False, ' | '.join(problems))
    detail = 'PT_LOAD alignments=' + ','.join(f'0x{x:x}' for x in aligns)
    if relro:
        detail += '; GNU_RELRO end aligned'
    else:
        detail += '; GNU_RELRO not present'
    return ElfResult(True, detail)


def local_data_offset(fp, info: zipfile.ZipInfo) -> int:
    fp.seek(info.header_offset)
    header = fp.read(30)
    if len(header) != 30:
        raise ValueError('truncated local ZIP header')
    sig, *_rest, name_len, extra_len = struct.unpack('<IHHHHHIIIHH', header)
    if sig != 0x04034B50:
        raise ValueError('invalid local ZIP header signature')
    return info.header_offset + 30 + name_len + extra_len


def abi_from_entry(filename: str) -> str | None:
    parts = filename.split('/')
    if len(parts) >= 3 and parts[0] == 'lib' and parts[-1].endswith('.so'):
        return parts[1]
    return None


def is_gating_abi(abi: str | None) -> bool:
    # Unknown layouts remain gating so an unexpected packaging layout cannot
    # silently bypass the acceptance check.
    return abi not in NON_GATING_ABIS


AUDIT_VERSION = "3"


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__.strip())
        return 2
    artifact = Path(sys.argv[1])
    if not artifact.is_file():
        print(f'ERROR: artifact not found: {artifact}')
        return 2
    suffix = artifact.suffix.lower()
    if suffix not in {'.apk', '.aab'}:
        print('ERROR: expected .apk or .aab')
        return 2

    print(f"16-KB audit version={AUDIT_VERSION} gateAbis=arm64-v8a,x86_64")
    failures = 0
    warnings = 0
    native_count = 0
    gating_native_count = 0
    packaged_graphics_path = []
    with artifact.open('rb') as raw, zipfile.ZipFile(raw) as zf:
        for info in zf.infolist():
            if not info.filename.endswith('.so'):
                continue
            native_count += 1
            if info.filename.rsplit('/', 1)[-1] == 'libandroidx.graphics.path.so':
                packaged_graphics_path.append(info.filename)
            abi = abi_from_entry(info.filename)
            gating = is_gating_abi(abi)
            if gating:
                gating_native_count += 1
            data = zf.read(info)
            elf = elf_16k(data)
            if elf.ok:
                print(f'OK ELF {info.filename}: {elf.detail}')
            else:
                status = 'FAIL' if gating else 'WARN'
                print(f'{status} ELF {info.filename}: {elf.detail}')
                if gating:
                    failures += 1
                else:
                    warnings += 1

            if suffix == '.apk':
                if info.compress_type == zipfile.ZIP_STORED:
                    try:
                        off = local_data_offset(raw, info)
                        zip_ok = off % PAGE == 0
                        if zip_ok:
                            print(f'OK ZIP  {info.filename}: dataOffset={off} mod16384={off % PAGE}')
                        else:
                            status = 'FAIL' if gating else 'WARN'
                            print(f'{status} ZIP  {info.filename}: dataOffset={off} mod16384={off % PAGE}')
                            if gating:
                                failures += 1
                            else:
                                warnings += 1
                    except Exception as exc:
                        status = 'FAIL' if gating else 'WARN'
                        print(f'{status} ZIP  {info.filename}: {exc}')
                        if gating:
                            failures += 1
                        else:
                            warnings += 1
                else:
                    print(f'WARN ZIP {info.filename}: compressed entry; direct mmap alignment is not applicable')
                    warnings += 1

    if packaged_graphics_path:
        print(
            'FAIL INVARIANT libandroidx.graphics.path.so is still packaged: ' +
            ', '.join(packaged_graphics_path)
        )
        failures += 1
    else:
        print('OK ABSENT libandroidx.graphics.path.so: Build 130 native-elimination invariant satisfied')

    if native_count == 0:
        print('OK: no native .so files found; app artifact is page-size agnostic at native layer')
    elif gating_native_count == 0:
        print('16-KB artifact audit FAILED: native libraries exist but no 64-bit gate ABI was found')
        return 1
    if suffix == '.aab':
        print('NOTE: AAB ELF checked. Also run: bundletool dump config --bundle=<aab> and require PAGE_ALIGNMENT_16K.')
    if failures:
        print(
            f'16-KB artifact audit FAILED: {failures} gating/invariant failure(s) across '
            f'{gating_native_count} 64-bit/unknown native libraries; {warnings} non-gating warning(s)'
        )
        return 1
    print(
        f'16-KB artifact audit OK: {gating_native_count} 64-bit/unknown native libraries gated, '
        f'{native_count} native libraries inspected, {warnings} non-gating warning(s)'
    )
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
