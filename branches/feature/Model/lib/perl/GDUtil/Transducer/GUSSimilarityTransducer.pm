#!/usr/bin/perl

#---------------------------------------------------------------------
# GUSSimilarityTransducer.pm
#
# Convert a set of GUS Similarity objects/rows to
# a clickable image.
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#---------------------------------------------------------------------

#
# TO DO
#  add ALT tag to IMG
#  use only as much vertical space as is needed

package WDK::Model::GDUtil::Transducer::GUSSimilarityTransducer;

use strict;

use DBI;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::StripeSpan;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::Packer;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_IMG_WIDTH = 400;
my $DFLT_IMG_HEIGHT = 300;
my $DFLT_JAVASCRIPT = 1;
my $DFLT_AUTO_HEIGHT = 1;
my $DFLT_TICK_INTERVAL = 'ends';

#-------------------------------------------------
# GUSSimilarityTransducer
#-------------------------------------------------

sub new {
    my($class, $args) = @_;

    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $queryTable = $args->{queryTable};
    my $subjectTable = $args->{subjectTable};
    my $filterTaxonIds = $args->{filterTaxonIds};
    my $js = $args->{javascript};
    my $autoheight = $args->{autoheight};
    my $tickInterval = $args->{tickInterval};
    my $subjExtDbIds = $args->{subjectExtDbIds};
    my $subjSimAlgInvIds = $args->{subjectSimAlgInvIds};
    my $subjectDb = $args->{subjectDb};

    my $self = {
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
	queryStart => $args->{queryStart},
	queryEnd => $args->{queryEnd},
	queryId => $args->{queryId},
	dbh => $args->{dbh},
	autoheight => (defined $autoheight) ? $autoheight : $DFLT_AUTO_HEIGHT,
	tickInterval => (defined $tickInterval) ? $tickInterval : $DFLT_TICK_INTERVAL,
	coreDb => $args->{coreDb},
	sresDb => $args->{sresDb},
	dotsDb => $args->{dotsDb},
    };
    
    bless $self, $class;

    $self->{queryTable} = $queryTable;
    $self->{queryTableId} = $self->getTableId($queryTable);
    $self->{subjectTable} = $subjectTable;
    $self->{subjectTableId} = $self->getTableId($subjectTable);
    $self->{subjectDb} = $subjectDb if (defined($subjectDb));
    $self->{filterTaxonIds} = $filterTaxonIds if (defined($filterTaxonIds));
    $self->{name} = $args->{name} if (defined($args->{name}));
    $self->{seqLabel} = $args->{seqLabel} if (defined($args->{seqLabel}));
    $self->{maxSubjects} = $args->{maxSubjects} if (defined($args->{maxSubjects}));
    $self->{hrefFn} = $args->{hrefFn} if (defined($args->{hrefFn}));
    $self->{hrefTarget} = $args->{hrefTarget} if (defined($args->{hrefTarget}));
    $self->{subjectExtDbIds} = $subjExtDbIds if (defined $subjExtDbIds);
    $self->{subjectSimAlgInvIds} = $subjSimAlgInvIds if (defined $subjSimAlgInvIds);

    my $rows = $self->getSimilarityRows();
    $self->processSimilarityRows($rows);

    return $self;
}

sub getHtml {
    my($self, $imgURL) = @_;

    my $w = $self->{width};
    my $h = $self->{height};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $html = '';

    if ($self->{javascript}) {
	$html .= "<script language=\"JavaScript1.1\">\n";
	$html .= "// <!--\n";
	$html .= <<ENDFUNC;

	function show${fnName}Info(descr, strand, pctId, pctPos, subject, subjLen) {
	    window.status = descr;

	    if (document.getElementById) {
		document.getElementById('${name}_defline').value = descr;
		document.getElementById('${name}_strand').value = strand;
		document.getElementById('${name}_pctid').value = pctId;
		document.getElementById('${name}_pctpos').value = pctPos;
		document.getElementById('${name}_subject').value = subject;
		document.getElementById('${name}_subjlen').value = subjLen;
		return true;
	    } 
	    else if (document.forms) {
		document.forms['${name}_form']['${name}_defline'].value = descr;
		document.forms['${name}_form']['${name}_strand'].value = strand;
		document.forms['${name}_form']['${name}_pctid'].value = pctId;
		document.forms['${name}_form']['${name}_pctpos'].value = pctPos;
		document.forms['${name}_form']['${name}_subject'].value = subject;
		document.forms['${name}_form']['${name}_subjlen'].value = subjLen;
		return true;
	    }
	    return false;
	}

ENDFUNC

	$html .= "// -->\n";
	$html .= "</script>\n";

	$html .= "<FORM ID=\"${name}_form\" NAME=\"${name}_form\">\n";
    }

    $html .= $self->getImageMap();
    $html .= "<IMG SRC=\"${imgURL}\" BORDER=\"0\" WIDTH=\"$w\" HEIGHT=\"$h\" ";
    $html .= "USEMAP=\"#${name}\">";

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TABLE BORDER=\"0\">\n";
	$html .= "<TR>";

	$html .= "<TD COLSPAN=\"5\">";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_defline\" ID=\"${name}_defline\" SIZE=\"75\"";
	$html .= " VALUE=\"Place the mouse over a similarity to see more detailed information here.\">";
	$html .= "</TD>\n";
	$html .= "<BR CLEAR=\"both\">\n";
	
	$html .= "</TR>";
	$html .= "<TR>";
	$html .= "<TD>Strand/frame:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_strand\" ID=\"${name}_strand\" SIZE=\"5\"></TD>\n";
	$html .= "<TD> Identical:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_pctid\" ID=\"${name}_pctid\" SIZE=\"5\"></TD>\n";
	$html .= "<TD> Positive:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_pctpos\" ID=\"${name}_pctpos\" SIZE=\"5\"></TD>\n";
	$html .= "<TD> Subject:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_subject\" ID=\"${name}_subject\" SIZE=\"21\"></TD>\n";
	$html .= "<TD> length:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_subjlen\" ID=\"${name}_subjlen\" SIZE=\"7\"></TD>\n";
	$html .= "</TR>";
	$html .= "</TABLE>\n";
	$html .= "</FORM>\n";
    }

    return $html;
}

sub getImageMap {
    my($self) = @_;
    my $name = $self->{name};

    my $im = '';

    $im .= "<MAP NAME=\"$name\">\n";
    $im .= $self->{rootSpan}->makeImageMap($self->{canvas});
    $im .= "</MAP>\n";

    return $im;
}

sub getPng {
    my($self) = @_;
    my $img = $self->{canvas}->getImage();
    return $img->can('png') ? $img->png() : $img->gif();
}

sub getJpeg {
    my($self, $quality) = @_;
    my $img = $self->{canvas}->getImage();
    if (!$img->can('jpeg')) { return $self->getPng(); }

    if (defined($quality)) {
	return $img->jpeg($quality);
    }
    return $img->jpeg();
}

sub processSimilarityRows {
    my($self, $rows) = @_;

    my $maxSubjs = $self->{maxSubjects};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $hrefFn = $self->{hrefFn};
    my $hrefTarget = $self->{hrefTarget};
    my $seqLabel = $self->{seqLabel};

    my $filterTaxonIds = $self->{filterTaxonIds};
    my $subjectImpTable = ($self->{subjectTable} =~ /nasequence/i) ? 'NASequenceImp' : 'AASequenceImp';

    # Whether to do filtering on NRDB taxon_id
    #
    my $filter = ($filterTaxonIds && ($subjectImpTable =~ /aaseq/i));
    my $fhash = undef;

    if ($filter) {
	$fhash = {};
	foreach my $tid (@$filterTaxonIds) {
	    $fhash->{$tid} = 1;
#	    print STDERR "GUSSimilarityTransducer: filtering taxon_id $tid\n";
	}
    }

    my $nSubjs = 0;
    
    my $canvas = 
	WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
			      {x1 => 250, x2 => $self->{width} - 20},
			      {x1 => $self->{queryStart}, x2 => $self->{queryEnd} });

    $canvas->allocateWebSafePalette();
    $self->{canvas} = $canvas;

    my $hspSpans = [];
    my $sbjSpans = [];
    my $currentSrcId = undef;
    my $row;
    my $lastRow;
    my $lastSimSpanId = undef;

    foreach $row (@$rows) {
	my($simSpanId, $srcId, $extDbId, $sbjLen, $descr, $sump, $sumrev, $minss, $maxse, 
	   $isrev, $qs, $qe, $ss, $se, $numId, $numPos, $frame, $matchlen, $score, 
	   $pvalMant, $pvalExp, $taxonId) = @$row;

#	print STDERR "GUSSimilarityTransducer: got row with span=$simSpanId taxon_id=$taxonId\n";

	# Skip hits to taxa that we're filtering out
	#
	if (defined($fhash) && ($taxonId =~ /\d/) && ($fhash->{$taxonId} == 1)) {
#	    print STDERR "GUSSimilarityTransducer: filtering row with taxon_id = $taxonId\n";
	    next;
	}
	
	# Skip duplicate rows introduced by join to NRDBEntry
	#
	if ($simSpanId == $lastSimSpanId) {
#	    print STDERR "GUSSimilarityTransducer: filtering row with duplicate span_id\n";
	    next;
	} else {
	    $lastSimSpanId = $simSpanId;
	}

	if ((not defined($currentSrcId)) || ($srcId ne $currentSrcId)) {
	    if (scalar(@$hspSpans) > 0) {
		push(@$sbjSpans, $self->makeSubjectSpan($hspSpans, $lastRow));
		++$nSubjs;
	    }

	    $currentSrcId = $srcId;
	    $hspSpans = [];
	    last if (defined($maxSubjs) && ($nSubjs == $maxSubjs));
	}

	$descr = &safeHtml($descr);

	my $strand = ($isrev ? '-' : '+');
	$strand .= $frame if (defined($frame));
	my $pctId = int(($numId / $matchlen) * 100.0 + 0.5);
	my $pctPos = int(($numPos / $matchlen) * 100.0 + 0.5);
	my $sbjC = "${ss}-${se} (${matchlen})";

	my($color, $fill) = &getHspColorAndFill($canvas, $sump);

	my $hspArgs = {
	    x1 => $qs,
	    x2 => $qe,
	    height => 10,
	    color => $color,
	    filled => $fill,
	    imagemapLabel => $descr,
	    shape => $isrev ? 'reverse' : 'forward'
	    };

	if ($self->{javascript}) {
	    my $mOver = "show${fnName}Info('${descr}', '${strand}', '${pctId}', '${pctPos}', '${sbjC}', '${sbjLen}'); return true;";
	    $hspArgs->{imagemapOnMouseOver} = $mOver;
	}

	$hspArgs->{imagemapHref} = &$hrefFn($srcId, $extDbId) if (defined($hrefFn));
	$hspArgs->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));

	push(@$hspSpans, WDK::Model::GDUtil::Span->new($hspArgs));
	$lastRow = $row;
    }

    if (scalar(@$hspSpans) > 0) {
	push(@$sbjSpans, $self->makeSubjectSpan($hspSpans, $lastRow));
    }

    my $qw = $self->{queryEnd} - $self->{queryStart};
    my $tLabel = ($self->{queryTable} =~ /aasequence/i) ? 'aa' : 'bp';

    $self->{rootSpan} = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => $self->{queryStart}, 
	  x2 => $self->{queryEnd}, 
	  y1 => $self->{height} - 5, 
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => $sbjSpans,
	  packer => WDK::Model::GDUtil::Packer::simplePacker(2),
	  tickInterval => $self->{tickInterval},
	  tickLabel => $tLabel,
	  label => defined($seqLabel) ? $seqLabel : ($self->{queryTable} . " " . $self->{queryId}),
	  labelVAlign => 'bottom'
      });
    
    $self->{rootSpan}->pack();

    # Ugly, but the only way
    #
    if ($self->{autoheight}) {
	$self->{height} = $self->{rootSpan}->getHeight() + 5;

	my $canvas = 
	  WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
				{x1 => 250, x2 => $self->{width} - 20},
				{x1 => $self->{queryStart}, x2 => $self->{queryEnd}});

	$canvas->allocateWebSafePalette();
	$self->{canvas} = $canvas;
	$self->{rootSpan}->{y1} = $self->{height} - 5;
	$self->{rootSpan}->pack();
    }

    $self->{rootSpan}->draw($self->{canvas});
}

my $cPacker = &WDK::Model::GDUtil::Packer::constantPacker(0);

sub makeSubjectSpan {
    my($self, $hspSpans, $row) = @_;

    my($simSpanId, $srcId, $extDbId, $sbjLen, $descr, $sump, $sumrev, $minss, $maxse, $isrev, $qs, $qe,
       $ss, $se, $numId, $numPos, $frame, $matchlen, $score, $pvalMant, $pvalExp, $taxonId) = @$row;

    $descr = &safeHtml($descr);

    my $coords = sprintf("[%.7s-%.7s]", $minss, $maxse);
    my $slabel = sprintf("%-15.15s %-8.1e %-.17s", $srcId, $sump, $coords);
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $args = {kids => $hspSpans,
		packer => $cPacker,
		label => $slabel,
		imagemapLabel => $descr,
	    };

    if ($self->{javascript}) {
	my $mOver = "show${fnName}Info('$descr', '', '', '', '', $sbjLen); return true;";
	$args->{imagemapOnMouseOver} = $mOver;
    }

    my $hrefFn = $self->{hrefFn};
    my $hrefTarget = $self->{hrefTarget};
    $args->{imagemapHref} = &$hrefFn($srcId, $extDbId) if (defined($hrefFn));
    $args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));

    my $ss = WDK::Model::GDUtil::StripeSpan->new($args);
    return $ss;
}

sub getTableId {
    my($self, $tableName) = @_;
    my $coreDb = $self->{coreDb};

    my $dbh = $self->{dbh};
    my $id;

    my $idSql = "select table_id from ${coreDb}.TableInfo where name = '$tableName'";
#    print STDERR "GUSSimilarity: sql='$idSql'\n";

    my $sth = $dbh->prepare($idSql);
    $sth->execute();

    while (my $row = $sth->fetchrow_arrayref()) { $id = $row->[0]; }

    $sth->finish();
    return $id;
}

sub getSimilarityRows {
    my($self) = @_;

    my $dotsDb = $self->{dotsDb};

    my $subjTableId = $self->{subjectTableId};
    my $queryTableId = $self->{queryTableId};
    my $queryId = $self->{queryId};
    my $extDbIds = $self->{subjectExtDbIds};
    my $subjectDb = $self->{subjectDb};
    my $algInvIds = $self->{subjectSimAlgInvIds};
    my $filterTaxonIds = $self->{filterTaxonIds};

    my $rows = [];
    
    my $subjectImpId = ($self->{subjectTable} =~ /nasequence/i) ? 'na_sequence_id' : 'aa_sequence_id';
    my $subjectImpTable = ($self->{subjectTable} =~ /nasequence/i) ? 'NASequenceImp' : 'AASequenceImp';

    # Whether to do filtering on NRDB taxon_id
    #
    my $filter = ($filterTaxonIds && ($subjectImpTable =~ /aaseq/i));

    my $domainSql = ("select distinct " .
		     "ss.similarity_span_id, " .
		     "si.source_id, " .
		     "si.external_database_release_id, " .
		     "si.length, " .
		     "si.description, " .
		     "TO_CHAR(s.pValue_mant, 'FM9999.9') || 'e' || TO_CHAR(s.pValue_exp, 'FMS9999') pValue, " .
		     "s.is_reversed, " .
		     "s.min_subject_start, " .
		     "s.max_subject_end, " .
		     "ss.is_reversed, " .
		     "ss.query_start, " .
		     "ss.query_end, " .
		     "ss.subject_start, " .
		     "ss.subject_end, " .
		     "ss.number_identical, " .
		     "ss.number_positive, " .
		     "ss.reading_frame, " .
		     "ss.match_length, " .
		     "s.score, " .
		     "s.pValue_mant, " .
		     "s.pValue_exp " .
		     ($filter ? ", ne.taxon_id " : "") .
		     "from ${dotsDb}.Similarity s, " .
		     "${dotsDb}.SimilaritySpan ss, " .
		     "${dotsDb}.$self->{subjectTable} si " .
		     ($filter ? ", ${dotsDb}.NRDBEntry ne " : "") .
		     "where s.subject_table_id = $subjTableId " .
		     "and s.query_table_id = $queryTableId " .
		     "and s.query_id = $queryId " .
		     "and ss.similarity_id = s.similarity_id " .
		     "and si.$subjectImpId = s.subject_id " .
		     (($subjectDb =~ /nrdb/) ? "and si.is_nrdb = 1 " : "") .
		     (defined($extDbIds) ? "and si.external_database_release_id in (" . join(',', @$extDbIds). ") " : "") .
		     (defined($algInvIds) ? "and s.row_alg_invocation_id in (" . join(',', @$algInvIds). ") " : "") .
		     ($filter ? "and si.aa_sequence_id = ne.aa_sequence_id (+) " : "") .
		     "order by s.pValue_exp asc, s.pValue_mant asc ");

#    print STDERR "GUSSimilarityTransducer: '$domainSql'\n";

    my $sth = $self->{dbh}->prepare($domainSql); 
    $sth->execute();

    while (my @row = $sth->fetchrow_array()) { 
	push(@$rows, \@row);
    }

    $sth->finish();
    return $rows;
}

#-------------------------------------------------
# File-scoped methods
#-------------------------------------------------

sub safeHtml {
    my($str) = @_;
    $str =~ s#<#&lt;#g;
    $str =~ s#>#&gt;#g;
    $str =~ s#'##g;
    return $str;
}

sub getHspColorAndFill {
    my($canvas, $pval) = @_;
    my $img = $canvas->getImage();

    if ($pval <= 1e-100) {
	return ($img->colorExact(255,0,0), 1);
    } elsif ($pval <= 1e-50) {
	return ($img->colorExact(255,153,153), 1);
    } elsif ($pval <= 1e-25) {
	return ($img->colorExact(255,204,204), 1);
    } elsif ($pval <= 1e-10) {
	return ($img->colorExact(153,153,153), 0);
    }
    return ($img->colorExact(204,204,204), 0);
}

1;

