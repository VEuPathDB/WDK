#!/usr/bin/perl

#------------------------------------------------------------------------
# BlastTransducer.pm
#
# Package used to convert WU-BLAST2 output to a clickable image.
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

# TO DO:
#  check axis labeling ('aa' for blastp and ??)
#  figure out HTML character encoding for apostrophe "'" (!) (safeHtml)
#  add color legend below the axis
#  add some dynamic options (?)
#  allow BLAST file to be in a separate linked window

package WDK::Model::GDUtil::Transducer::BlastTransducer;

use strict;

use BLAST2::BLAST2;
use BLAST2::SBJCT;
use BLAST2::HSP;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::StripeSpan;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_IMG_WIDTH = 400;
my $DFLT_IMG_HEIGHT = 300;
my $DFLT_JAVASCRIPT = 1;

#-------------------------------------------------
# BlastTransducer
#-------------------------------------------------

sub new {
    my($class, $args) = @_;

    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $js = $args->{javascript};

    my $self = {
	blastFile => $args->{blastFile},
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
    };

    bless $self, $class;
    $self->{name} = $args->{name} if (defined($args->{name}));
    $self->{seqUrlFn} = $args->{seqUrlFn} if (defined($args->{seqUrlFn}));
    $self->parseBlastFile();
    return $self;
}

sub parseBlastFile {
    my($self) = @_;
    my $blastFile = $self->{blastFile};
    my $name = $self->{name};

    if (-e $blastFile) {
	my $b = &BLAST2::parseBLAST2output($blastFile);
	$self->{blastObj} = $b;

	my $canvas = 
	  WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
				{x1 => 250, x2 => $self->{width} - 25},
				{x1=>0, x2 => $b->{'query_size'}});
	$canvas->allocateWebSafePalette();
	$self->{canvas} = $canvas;

	my $subjSpans = [];
	my $nSubjs = $b->getNumSbjcts();

	for (my $s = 0;$s < $nSubjs;++$s) {
	    my $sbj = $b->getSbjct($s);

	    my $descr = &safeHtml($sbj->{'description'});
	    $descr =~ s/\s+/ /g;
	    my $sbjLen = $sbj->{'length'};
	    
	    my $hspSpans = [];
	    my $nHsps = $sbj->getNumHSPs();
	    my($bestPval, $minss, $maxse);

	    for (my $h = 0;$h < $nHsps;++$h) {
		my $hsp = $sbj->getHSP($h);

		my $pctId = int(($hsp->{'identities'} / $hsp->{'length'}) * 100.0 + 0.5);
		my $pctPos = int(($hsp->{'positives'} / $hsp->{'length'}) * 100.0 + 0.5);
		my $strand = $hsp->{'strand'};
		
		# Summary statistics
		#
		if ($h == 0) {
		    $bestPval = $hsp->{pval};
		    $minss = $hsp->{s_start};
		    $maxse = $hsp->{s_end};
		} else {
		    $bestPval = $hsp->{pval} if ($hsp->{pval} < $bestPval);
		    $minss = $hsp->{s_start} if ($hsp->{s_start} < $minss);
		    $maxse = $hsp->{s_end} if ($hsp->{s_end} > $maxse);
		}

		# TO DO - put this in a function
		# 
		my $hitLen = $hsp->{length};
		my $sbjC = $hsp->{'s_start'} . "-" . $hsp->{'s_end'} . " ($hitLen)";
		my($color, $fill) = &getHspColorAndFill($canvas, $hsp);
		my($descrPrefix) = ($descr =~ /^(\S*)/);
		my($qs, $qe);
		$qs = $hsp->{q_start};
		$qe = $hsp->{q_end};

		if ($qs > $qe) {
		    my $tmp = $qs;
		    $qs = $qe;
		    $qe = $tmp;
		}

		my $args = {x1 => $qs, x2 => $qe,
			    height => 7,
			    color => $color,
			    filled => $fill,
			    imagemapLabel => $descr,
			    imagemapHref => "#$descrPrefix",
			    shape => ($hsp->{strand} =~ /minus|\-\d/) ? 'reverse' : 'forward',
			    };

		if ($self->{javascript}) {
		    my $mOver = 
			("window.status = '$descr';" .
			 "document.forms['${name}_form']['${name}_defline'].value='$descr';" .
			 "document.forms['${name}_form']['${name}_strand'].value='${strand}';" .
			 "document.forms['${name}_form']['${name}_pctid'].value='${pctId}%';" .
			 "document.forms['${name}_form']['${name}_pctpos'].value='${pctPos}%';" .
			 "document.forms['${name}_form']['${name}_subject'].value='${sbjC}';" .
			 "document.forms['${name}_form']['${name}_subjlen'].value='${sbjLen}';" .
			 "return true;");
		    $args->{imagemapOnMouseOver} = $mOver;
		}

		push(@$hspSpans, WDK::Model::GDUtil::Span->new($args));
	    }

	    my $coords = sprintf("[%.7s-%.7s]", $minss, $maxse);
	    my $slabel = sprintf("%-15.15s %-8.1e %-.17s", $descr, $bestPval, $coords);

	    push(@$subjSpans, 
	       WDK::Model::GDUtil::StripeSpan->new({kids => $hspSpans,
					packer => &WDK::Model::GDUtil::Packer::constantPacker(0),
					label => $slabel
					}));
	}
	
	my $prog = $self->{blastObj}->{'program'};
	my $tl = ($prog =~ /blastp/i) ? 'aa' : 'bp';

	$self->{rootSpan} = 
	  WDK::Model::GDUtil::AxisSpan->new({
	      x1 => 0, x2 => $b->{'query_size'}, 
	      y1 => $self->{height} - 10, 
	      height=>6, tickHeight=>4, tickWidth=>1,
	      kids => $subjSpans,
	      packer => WDK::Model::GDUtil::Packer::simplePacker(4),
	      tickLabel => $tl,
	      label => sprintf("%30.30s", $b->{query}),
	      labelVAlign => 'bottom'
	  });
	
	$self->{rootSpan}->pack();
	$self->{rootSpan}->draw($canvas);
    } else {
	print STDERR "BlastTransducer: $blastFile does not exist\n";
    }
}

sub getHtml {
    my($self, $imgURL) = @_;
    my $html = '';

    my $w = $self->{width};
    my $h = $self->{height};
    my $name = $self->{name};
    my $queryName = $self->{blastObj}->{'query'};
    my $dbName = $self->{blastObj}->{'db'};
    my $program = $self->{blastObj}->{'program'};

    $html .= "<FORM NAME=\"${name}_form\">\n";

    $html .= $self->getImageMap();

    $html .= "<A NAME=\"${name}_top\">\n";
    $html .= "<HR HEIGHT=\"1\">";
    $html .= "<B>$program:</B> $queryName vs. $dbName<BR><BR>\n";

    $html .= ("<I>BLAST hits are ordered by sum-P, with the most significant hits " .
	      "at the bottom of the graphic.  Hits against the same database sequence " .
	      "are shown on the same line.  Arrows on the end of the HSPs indicate " .
	      "the strand/reading frame of the hit.  Position the mouse over an HSP to see " .
	      "the details of the alignment (requires JavaScript); click on an HSP " .
	      "to view the appropriate section of the BLAST output.</I>");

    $html .= "<BR><BR>\n";

    $html .= "<IMG SRC=\"${imgURL}\" BORDER=\"1\" ";
    $html .= "WIDTH=\"$w\" HEIGHT=\"$h\" USEMAP=\"#${name}\">\n";
    $html .= "<BR CLEAR=\"both\">\n";

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TABLE BORDER=\"0\">\n";
	$html .= "<TR>";

	$html .= "<TD COLSPAN=\"5\">";
	$html .= "<INPUT TYPE=\"text\" NAME=\"${name}_defline\" SIZE=\"75\">";
	$html .= "</TD>\n";

	$html .= "</TR>";
	$html .= "<TR>";
	$html .= "<TD>Strand/frame:<INPUT TYPE=\"text\" NAME=\"${name}_strand\" SIZE=\"5\"></TD>\n";
	$html .= "<TD> Identical:<INPUT TYPE=\"text\" NAME=\"${name}_pctid\" SIZE=\"3\"></TD>\n";
	$html .= "<TD> Positive:<INPUT TYPE=\"text\" NAME=\"${name}_pctpos\" SIZE=\"3\"></TD>\n";
	$html .= "<TD> Subject:<INPUT TYPE=\"text\" NAME=\"${name}_subject\" SIZE=\"21\"></TD>\n";
	$html .= "<TD> length:<INPUT TYPE=\"text\" NAME=\"${name}_subjlen\" SIZE=\"7\"></TD>\n";
	$html .= "</TR>";
	$html .= "</TABLE>\n";
    }

    $html .= "</FORM>\n";

    $html .= "<FONT SIZE=\"-1\">";
    $html .= "<I>BLAST graphical summary generated by BlastTransducer.pm, ";
    $html .= "using Lincoln Stein's GD.pm</I>";
    $html .= "</FONT>\n";

    $html .= "<HR HEIGHT=\"1\">";
    $html .= "<PRE>\n";

    my $sfun = $self->{'seqUrlFn'};

    open(BF, $self->{'blastFile'});

    while(<BF>) {
	if (/^>((\S*).*)$/) {
	    $html .= "<A NAME=\"$2\">";

	    if (defined($sfun)) {
		my $surl = &$sfun($1);
		$html .= "&lt;<A TARGET=\"seq\" HREF=\"$surl\">sequence</A>&gt; ";
	    }
	    $html .= "&lt;<A HREF=\"#${name}_top\">back</A>&gt;\n\n";
	    $html .= ">$1\n";
	} else {
	    $html .= $_;
	}
    }

    close(BF);
    $html .= "</PRE>\n";

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
    return $self->{canvas}->getImage()->png();
}

sub getJpeg {
    my($self) = @_;
    if (defined($quality)) {
	return $self->{canvas}->getImage()->jpeg($quality);
    }
    return $self->{canvas}->getImage()->jpeg();
}

#-------------------------------------------------
# File-scoped methods
#-------------------------------------------------

sub safeHtml {
    my($str) = @_;
    $str =~ s#<#&lt;#;
    $str =~ s#>#&gt;#;
    $str =~ s#'##;
    return $str;
}

sub getHspColorAndFill {
    my($canvas, $hsp) = @_;
    my $img = $canvas->getImage();
    my $pval = $hsp->{pval};

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

