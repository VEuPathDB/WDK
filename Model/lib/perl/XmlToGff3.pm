#!/usr/bin/perl

package WDK::Model::XmlToGff3;

use strict;
use Carp;
use XML::Simple;
use CGI;

my @GFFCols = ('seqid','source','type','fstart','fend','score','strand','phase');
my $debug = 0;

# my $str = 'test; cgi';
# my $esc_str = CGI->escape($str);
# print "\n\"$str\" escaped into \"$esc_str\"\n\n";

sub convert {
  my ($inFile, $outFile, $hasSeq) = @_;

  if (!$outFile) {
    $outFile = $inFile;
    $outFile =~ s/xml$/gff/i;
    if ($outFile eq $inFile) { $outFile = $inFile . '.gff'; }
  }

  my $conv = new XML::Simple(ForceArray => 1, KeepRoot => 1);
  my $in = $conv->XMLin($inFile);

  open OUT, ($hasSeq ? '>>' : '>') . $outFile
    or &confess("could not open $outFile for write: $!");
  select OUT;

  if ($hasSeq) {
    &_processNode($in, 1, 0); # first pass for GFF lines
    print "\n##FASTA\n";
    &_processNode($in, 1, 1); # second pass for FASTA section
  } else {
    print "##gff-version 3\n";
    &_processNode($in, 0);
  }

  select STDOUT;
  close OUT;
}


sub _processNode {
  my ($nodeRef, $hasSeq, $onlySeq) = @_;

  if (ref($nodeRef) ne 'HASH') { return; }

  foreach my $cn (keys %$nodeRef) {
    print "DEBUG " . $cn . "\n" if $debug;
    my $childNodes = $nodeRef->{$cn};

    foreach my $childNode (@$childNodes) {
      if ($cn eq 'li') {
	print "DEBUG found li tag\n" if $debug;

        my $fstart = $childNode->{$GFFCols[3]}->[0];
        my $fend = $childNode->{$GFFCols[4]}->[0];
        if ($fstart > $fend) {
            $childNode->{$GFFCols[3]}->[0] = $fend;
            $childNode->{$GFFCols[4]}->[0] = $fstart;
        }

	foreach (@GFFCols) {
	  my $col = $childNode->{$_}->[0];
	  print "DEBUG $_ = $col\n" if $debug;
	  print "$col\t" unless $onlySeq;
	}

	print 'ID=' . $childNode->{'attr_id'}->[0] if !$onlySeq;
	my $lastSeqId = $childNode->{$GFFCols[0]}->[0];

	my @nestedLis;
	foreach my $gcn (keys %$childNode) {
	  print "DEBUG found $gcn tag within li tag\n" if $debug;
	  my $grandChild = $childNode->{$gcn}->[0];
	  if (ref($grandChild) eq 'HASH') {
	    push @nestedLis, $grandChild;
	  } elsif ($gcn =~ /^attr\_(\S+)$/) {
	    my $attr = &_normalizeAttr($1);
	    $grandChild = CGI->escape($grandChild) if $1 =~ /^(description|name)$/;
	    print ';' . $attr . '=' . $grandChild unless ($attr eq 'ID' or $onlySeq);
	  } elsif ($gcn =~ /^sequence$/) {
	    if ($onlySeq) { print ">$lastSeqId\n" . &_formatSeq($grandChild, 80) . "\n"; }
	  } else {
	    print "DEBUG ignored tag $gcn\n" if $debug;
	  }
	}
	print "\n";
	foreach (reverse @nestedLis) { &_processNode($_, $hasSeq, $onlySeq); }
      } else {
	&_processNode($childNode, $hasSeq, $onlySeq);
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

sub _formatSeq {
  my ($seq, $width) = @_;
  $width = 80 unless $width;
  $seq =~ s/\s+//gm;
  my $len = length $seq;
  my $num_lines = 1+ $len/$width;
  my $fmtSeq = "";
  for (my $i=0; $i<$num_lines; $i++) {
    my $offset = $width * $i;
    $fmtSeq .= substr($seq, $offset, $width) . "\n";
  }
  return $fmtSeq;
}

1;

