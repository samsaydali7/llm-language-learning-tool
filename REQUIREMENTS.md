# Language Learning App — Requirements

## Version

V1.0

## Status

Final

## Purpose

The application will be a private language-learning app that turns user-owned books and their accompanying audio into organized learning material.

The application should be designed so that other languages and books can be added later, provided the user owns or is otherwise authorized to use the material.

The application should help the user:

Understand and organize the content of a book.
Study vocabulary and grammar.
Practice by topic.
Complete exercises.
Review mistakes.
Use flashcards.
Practice listening.
Review grammar with examples.
Follow a daily review routine.

## Languages

The application must not be limited to French.

The user should be able to:

Add a language.
Select the language they want to learn.
Add multiple learning languages in the future.
Choose the language in which explanations are presented.

For example:

Learning language: French
Explanation language: English

Later:

Learning language: Hungarian
Explanation language: English

## Books and Learning Materials

The user should be able to add a book or other learning source only when they own it or are otherwise authorized to use it for personal study.

The system must not be used to store copyrighted material without permission.

A book can contain:

A PDF
Audio files
Other supporting files in the future

For the first version, the user will provide:

The complete book PDF
The accompanying audio files

The application should treat the book and its audio as parts of the same learning source.

## Book Organization

The application should understand the organization of the book.

A book can contain:

Books/volumes
Chapters
Sections
Subsections
Further levels of organization

The application should preserve this structure.

For example:

French for Dummies 6-in-1
→ Book 2
→ Chapter 4
→ At the Restaurant
→ Ordering Food

The user should be able to browse the material using this structure.

## Audio

The user should be able to upload the audio associated with the book.

The application should connect audio files with the appropriate parts of the book whenever possible.

The book already contains the written transcripts of the audio.

Therefore, the application should use those transcripts rather than requiring speech-to-text processing.

The user should be able to:

Play an audio track.
See which part of the book it belongs to.
View its transcript.
Use the audio later for listening exercises.

## Understanding the Book

The application should analyze the book and identify useful learning content.

For each relevant part of the book, it should identify things such as:

Vocabulary
Words
Meanings
Relevant grammatical information
Examples
Grammar
Grammar concepts
Rules
Patterns
Examples
Important notes
Expressions
Common expressions
Useful phrases
Their meanings
How they are used
Examples

The application should preserve useful examples from the book.

Topics

The application should identify topics such as:

Greetings
Family
Restaurants
Travel
Shopping
Work
Transportation
Hotels

The exact topics should come from the content rather than being restricted to a predefined list.

## Source References

Learning information should remain connected to its original location in the book.

For example:

Vocabulary: réservation

Book: French for Dummies — Book 2
Chapter: At the Restaurant
Page: 215

The user should be able to go back to the original source.

This is important because the book remains the primary learning source.

## Topics

The application should allow learning by topic, independently of the book's chapter structure.

For example, the user could select:

Restaurant

and receive relevant material from multiple chapters or books.

A topic could contain:

Vocabulary
Grammar
Expressions
Examples
Exercises
Flashcards
Listening material

## Choosing What to Study

The user should be able to decide how broad or narrow their study session is.

For example:

Everything

French → French for Dummies

One book

French → French for Dummies → Book 2

One chapter

Book 2 → Chapter 4

One section

Chapter 4 → At the Restaurant

One topic

Restaurant

One type of knowledge

Grammar

or:

Vocabulary

The user should be able to combine these choices.

For example:

French
French for Dummies
Book 2
Topic: Restaurant
Grammar + Vocabulary

## Exercises

The application should generate exercises based on the selected learning material.

Exercises should be based on what the user has selected, rather than random information from the entire book.

For example:

Topic: Restaurant
Vocabulary + Grammar
20 exercises

The application should create exercises appropriate to the selected material.

Possible exercise types include:

Multiple choice
Fill in the blank
Translation
Matching
Sentence ordering
Sentence construction
Grammar transformation

More exercise types can be added later.

## Exercise Answers

Every exercise should have a correct answer.

After answering, the user should be able to see:

Whether their answer was correct.
The correct answer.
