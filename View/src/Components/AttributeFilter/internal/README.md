Modules in this directory are not eagerly loaded by webpack, unlike modules in
the parent directory. This allows us to lazy load modules in this directory. It
also allows us to cherrypick which components from this directory are included
in the parent directory (see AttributeFilter and ServerSideAttributeFilter as an
example).
