Generate complete documentation for the COBOL program: $ARGUMENTS

## Steps

1. Verify the source file exists:
   `source/cobol/$ARGUMENTS.cbl` or `source/cobol/$ARGUMENTS.CBL`
   If not found, stop and list: `ls source/cobol/*.cbl source/cobol/*.CBL`

2. Extract the IR (Intermediate Representation):
   ```bash
   python output/business-docs/tools/cobol_ir.py $ARGUMENTS
   ```
   This writes `output/ir/$ARGUMENTS.json` — a structured extract of all paragraphs,
   data items, COPY resolutions, PERFORM graph, COMP-3 fields, and unreferenced fields.
   Read this JSON file before generating the BIZ-*.md — it is the ground truth for line
   numbers, field names, byte counts, and unused fields. Do not guess values that are in the IR.

3. Use the `documenter` agent:
   The documenter agent reads the COBOL source, all copybooks, AND `output/ir/$ARGUMENTS.json`,
   then writes the full BIZ-*.md.
   Alternatively, perform the documentation yourself by following `.claude/agents/documenter.md`.

4. After writing `output/business-docs/$ARGUMENTS/BIZ-$ARGUMENTS.md`, convert it:
   ```bash
   cd output/business-docs
   python tools/_md_to_docx.py $ARGUMENTS/BIZ-$ARGUMENTS.md $ARGUMENTS/BIZ-$ARGUMENTS.docx
   ```

5. Report: file path, line count of the .md, and whether the DOCX was created.

## Reference files
- Standard: `output/business-docs/DOCUMENTATION-STANDARD.md`
- Example:   `output/business-docs/CBACT01C/BIZ-CBACT01C.md`
- Template:  `output/business-docs/TEMPLATE.md`
- IR tool:   `output/business-docs/tools/cobol_ir.py`
