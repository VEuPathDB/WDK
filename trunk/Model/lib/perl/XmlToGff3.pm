#!/usr/bin/perl

package WDK::Model::XmlToGff3;

use strict;
use Carp;
use XML::Simple;

my @GFFCols = ('seqid','source','type','fstart','fend','score','strand','phase');
my $debug = 0;

sub convert {
  my ($inFile, $outFile) = @_;

  if (!$outFile) {
    $outFile = $inFile;
    $outFile =~ s/xml$/gff/i;
    if ($outFile eq $inFile) { $outFile = $inFile . '.gff'; }
  }

  my $conv = new XML::Simple(ForceArray => 1, KeepRoot => 1);
  my $in = $conv->XMLin($inFile);

  open OUT, ">$outFile" or &confess("could not open $outFile for write: $!");
  select OUT;
  &_processNode($in);
  select STDOUT;
  close OUT;
}


sub _processNode {
  my $nodeRef = shift;
  if (ref($nodeRef) ne 'HASH') { return; }

  foreach my $cn (keys %$nodeRef) {
    print "DEBUG " . $cn . "\n" if $debug;
    my $childNodes = $nodeRef->{$cn};

    foreach my $childNode (@$childNodes) {
      if ($cn eq 'li') {
	print "DEBUG found li tag\n" if $debug;
	foreach (@GFFCols) {
	  my $col = $childNode->{$_}->[0];
	  print "DEBUG $_ = $col\n" if $debug;
	  print "$col\t";
	}
	print 'ID=' . $childNode->{'attr_id'}->[0];

	my @nestedLis;
	foreach my $gcn (keys %$childNode) {
	  print "DEBUG found $gcn tag within li tag\n" if $debug;
	  my $grandChild = $childNode->{$gcn}->[0];
	  if (ref($grandChild) eq 'HASH') {
	    push @nestedLis, $grandChild;
	  } elsif ($gcn =~ /^attr\_(\S+)$/) {
	    my $attr = &_normalizeAttr($1);
	    print ';' . $attr . '=' . $grandChild unless $attr eq 'ID';
	  } else {
	    print "DEBUG ignored tag $gcn\n" if $debug;
	  }
	}
	print "\n";
	foreach (@nestedLis) { &_processNode($_); }
      } else {
	&_processNode($childNode);
      }
    }
  }
}

sub _normalizeAttr {
  my $attr = shift;
  $attr = 'ID' if $attr eq 'id';
  $attr = 'Name' if $attr eq 'name';
  $attr = 'Alias' if $attr eq 'alias';
  $attr = 'Parent' if $attr eq 'parent';
  $attr = 'Tarent' if $attr eq 'target';
  $attr = 'Gap' if $attr eq 'gap';
  $attr = 'Derived_from' if $attr eq 'derived_from';
  $attr = 'Note' if $attr eq 'note';
  $attr = 'Dbxref' if $attr eq 'dbxref';
  $attr = 'Ontology_term' if $attr eq 'ontology_term';
  return $attr;
}

1;

