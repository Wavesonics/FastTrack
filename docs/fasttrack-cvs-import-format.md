# FastTrack CSV Import Format

The import file must be a plain comma-delimited CSV with this exact header line:

```
ID,Start Date,Start Time,Duration (hours)
```

## Columns

| Column               | Format        | Notes                                                                                                                                    |
|----------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| **ID**               | integer       | Must be unique per row. On import, an existing entry with the same ID is deleted and replaced — duplicate IDs will overwrite each other. |
| **Start Date**       | `yyyy-MM-dd`  | e.g. `2026-07-16`                                                                                                                        |
| **Start Time**       | `H:mm`        | 24-hour. Hour not zero-padded, minute zero-padded to 2 digits. e.g. `9:05`, `14:30`                                                      |
| **Duration (hours)** | whole integer | Hours only, no decimals or minutes. A non-integer (e.g. `12.5`) causes the row to be silently skipped.                                   |

## Example

```csv
ID,Start Date,Start Time,Duration (hours)
1,2026-07-14,8:00,16
2,2026-07-15,20:30,18
3,2026-07-16,9:05,20
```

## Rules & gotchas

- **No quoting/escaping.** Fields are split on plain commas — quoted fields and escaped commas are
  not supported. Keep all values comma-free.
- **Header is mandatory.** The first line is always skipped. A file with only a header (or empty)
  fails.
- **Each row needs at least 4 fields**, or it's skipped. Extra trailing columns are ignored.
- **Duration must be a whole number of hours** — fractional durations can't be represented.
- **Time zone:** import parses `Start Time` as **UTC**. If your source times are local wall-clock
  times, they'll be stored offset by your UTC difference — adjust beforehand if that matters.
- Line endings: rows split on newlines; blank lines and surrounding whitespace are ignored.