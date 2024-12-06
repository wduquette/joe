# Expander

This was an interesting trip down memory lane, but it's a dead end,
at least as it currently stands.  

The Tcl `expand` program came out of a desire to produce arbitrary
documents using _ad hoc_ "rules", written as Tcl commands.  I later used
it as a site generator, with a set of "rules" of ever-increasing
complexity. In the modern day, I'd be much better off writing a site
generator that builds the kind of site I want; and then seeing how 
I want to make it scriptable.

This would probably include:

- The ability to include expressions, as in `{{Joe expression}}`, to
  include data from the site's data model into the file or template.
- The ability to define custom markup, as in `<<myTag ...>>...<</myTag>>`,
  where the tag is implemented in Joe but the context stack, etc., is
  handled implicitly.  (E.g., if this is a pair of tags, one specifies
  how the text between them is handled.)
