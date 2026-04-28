Generate complete documentation for the COBOL program: $ARGUMENTS

## Steps

1. Verify the source file exists:
   `source/cobol/$ARGUMENTS.cbl` or `source/cobol/$ARGUMENTS.CBL`
   If not found, stop and list: `ls source/cobol/*.cbl source/cobol/*.CBL`

2. Use the `documenter` agent:
   The documenter agent reads the COBOL source and all its copybooks, then writes the full BIZ-*.md.
   Alternatively, perform the documentation yourself by following `.claude/agents/documenter.md`.

3. After writing `output/business-docs/$ARGUMENTS/BIZ-$ARGUMENTS.md`, convert it:
   ```bash
   cd output/business-docs
   python tools/_md_to_docx.py $ARGUMENTS/BIZ-$ARGUMENTS.md $ARGUMENTS/BIZ-$ARGUMENTS.docx
   ```

4. Report: file path, line count of the .md, and whether the DOCX and PNG were created.

## Reference files
- Standard: `output/business-docs/DOCUMENTATION-STANDARD.md`
- Example:   `output/business-docs/CBACT01C/BIZ-CBACT01C.md`
- Template:  `output/business-docs/TEMPLATE.md`
