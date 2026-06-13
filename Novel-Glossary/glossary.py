import re
import os
from ebooklib import epub


# =========================
# CONFIG (EDIT ONLY THIS)
# =========================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

INPUT_FILE = os.path.join(BASE_DIR, "input", "Chapter 24 -31.txt")

OUTPUT_FILE = os.path.join(
    os.path.dirname(BASE_DIR),
    "novels",
    "output.epub"
)

BOOK_TITLE = "My Converted Book"

os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
GLOSSARY = {
    "Bai Qiusheng": "Issac",
    "vM7fi": "^"
}

## Rule if character "^" then the first term is deleted

# =========================
# TEXT PROCESSOR
# =========================

def apply_glossary(text, glossary):
    for target, replacement in glossary.items():

        # DELETE RULE
        if replacement == "^":
            pattern = r"\b" + re.escape(target) + r"\b"
            text = re.sub(pattern + r"\s*", "", text)

        # REPLACE RULE
        else:
            pattern = r"\b" + re.escape(target) + r"\b"
            text = re.sub(pattern, replacement, text)

    # cleanup spacing issues caused by deletions
    text = re.sub(r"[ ]{2,}", " ", text)
    text = re.sub(r"\n{3,}", "\n\n", text)

    return text.strip()


# =========================
# EPUB CREATOR
# =========================

def txt_to_epub(input_txt_path, output_epub_path, title):
    with open(input_txt_path, "r", encoding="utf-8") as f:
        raw_text = f.read()

    processed_text = apply_glossary(raw_text, GLOSSARY)

    book = epub.EpubBook()
    book.set_title(title)
    book.set_language("en")

    chapter = epub.EpubHtml(title="Chapter 1", file_name="chap_01.xhtml")
    chapter.content = (
        f"<h1>{title}</h1><p>{processed_text.replace('\n', '<br>')}</p>"
    )

    book.add_item(chapter)

    book.toc = (epub.Link("chap_01.xhtml", "Chapter 1", "chap1"),)
    book.spine = ["nav", chapter]

    book.add_item(epub.EpubNcx())
    book.add_item(epub.EpubNav())

    epub.write_epub(output_epub_path, book, {})

    print(f"EPUB created: {output_epub_path}")


# =========================
# RUN
# =========================

if __name__ == "__main__":
    txt_to_epub(INPUT_FILE, OUTPUT_FILE, BOOK_TITLE)