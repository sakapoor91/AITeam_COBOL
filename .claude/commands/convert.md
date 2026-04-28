Convert an existing BIZ-*.md to Word (.docx) and Mermaid diagram (.png): $ARGUMENTS

## Steps

1. Verify the .md exists:
   `output/business-docs/$ARGUMENTS/BIZ-$ARGUMENTS.md`
   If not found, run `/document $ARGUMENTS` first.

2. Run the converter:
   ```bash
   cd output/business-docs
   python tools/_md_to_docx.py $ARGUMENTS/BIZ-$ARGUMENTS.md $ARGUMENTS/BIZ-$ARGUMENTS.docx
   ```
   The Mermaid PNG (`BIZ-$ARGUMENTS-flow.png`) is generated automatically alongside the DOCX.

3. If you want to regenerate ALL programs at once:
   ```bash
   cd output/business-docs
   python tools/generate_all.py           # skips up-to-date DOCX files
   python tools/generate_all.py --force   # regenerate everything
   ```

## Prerequisites
- Python: `pip install python-docx`
- Mermaid CLI (for PNG): `npm install -g @mermaid-js/mermaid-cli`
  Without mmdc the DOCX is still generated; a text callout replaces the diagram.
