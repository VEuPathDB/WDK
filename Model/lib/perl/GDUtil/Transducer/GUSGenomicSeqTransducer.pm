#!/usr/bin/perl

#------------------------------------------------------------------------
# GUSGenomicSeqTransducer.pm
#
# Display a graphical representation of a genomic sequence, including gene 
# predictions and other annotation.  Currently highly PlasmoDB-specific.
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::GUSGenomicSeqTransducer;

use strict;
use DBI;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::StripeSpan;
use WDK::Model::GDUtil::HorizLineSpan;
use WDK::Model::GDUtil::VertLineSpan;
use WDK::Model::GDUtil::PercentATSpan;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::Packer;

use WDK::Model::GDUtil::Transducer::GUSSimilarityTransducer;
use WDK::Model::GDUtil::Transducer::GUSSimilarityQueryBuilder;
use WDK::Model::GDUtil::Transducer::DEBUG;
use WDK::Model::GDUtil::Transducer::MyEscape;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $SHOW_TIMING = 0;
my $debug = 0;

my $DFLT_IMG_WIDTH = 400;
my $DFLT_IMG_HEIGHT = 300;
my $DFLT_JAVASCRIPT = 1;
my $DFLT_AUTO_HEIGHT = 1;
my $DFLT_USE_SCROLLBAR = 0;
my $DFLT_TICK_INTERVAL = 10000;

#-------------------------------------------------
# Configuration
#-------------------------------------------------

# HACK - This code is specific to PlasmoDB
#
my $PLASMODIUM_SPECIES = [
			  { 
			      display_name => 'P. falciparum',
			      taxon_ids => [211],
			      have_genomic => 1,
			      pval_cutoff => -50,
			      have_est_gss => 1,
			      twoletter => 'pf',
			  },
			  { 
			      display_name => 'P. yoelii',
			      taxon_ids => [2736,87262],
			      have_genomic => 1,
			      pval_cutoff => -50,
			      have_est_gss => 1,
			      twoletter => 'py',
			  },
			  { 
			      display_name => 'P. knowlesi',
			      taxon_ids => [1070],
			      have_genomic => 1,
			      pval_cutoff => -50,
			      twoletter => 'pk',
			  },
			  { 
			      display_name => 'P. chabaudi',
			      taxon_ids => [260],
			      have_genomic => 1,
			      pval_cutoff => -50,
			      have_est_gss => 1,
			      twoletter => 'pc',
			  },
			  { 
			      display_name => 'P. berghei',
			      taxon_ids => [636],
			      have_genomic => 0,
			      pval_cutoff => -50,
			      have_est_gss => 1,
			      twoletter => 'pb',
			  },
			  { 
			      display_name => 'P. vivax',
			      taxon_ids => [2788],
			      have_genomic => 1,
			      pval_cutoff => -50,
			      have_est_gss => 1,
			      twoletter => 'pv',
			  },
			  ];

#-------------------------------------------------
# GUSGenomicSeqTransducer
#-------------------------------------------------

sub new {
    my($class, $args) = @_;
    
    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $naSeqId = $args->{naSeqId};
    my $js = $args->{javascript};
    my $autoheight = $args->{autoheight};
    my $organism = $args->{organism};
    my $chromosome = $args->{chromosome};
    my $useScrollbar = $args->{useScrollbar};
    my $tickInterval = $args->{tickInterval};
    my $showInconsist = $args->{showInconsistentAlignments};

    my $self = {
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
	naSeqId => $naSeqId,
	dbh => $args->{dbh},
	autoheight => (defined $autoheight) ? $autoheight : $DFLT_AUTO_HEIGHT,
	useScrollbar => (defined $useScrollbar) ? $useScrollbar : $DFLT_USE_SCROLLBAR,
	tickInterval => (defined $tickInterval) ? $tickInterval : $DFLT_TICK_INTERVAL,
	coreDb => $args->{coreDb},
	sresDb => $args->{sresDb},
	dotsDb => $args->{dotsDb},
	radDb => $args->{radDb},    # needed for SAGE tage spans
    };
    
    bless $self, $class;

    $self->{showInconsistentAlignments} = $showInconsist if (defined($showInconsist));
    $self->{chromosome} = $chromosome if (defined($chromosome));
    $self->{name} = $args->{name} if (defined($args->{name}));
    # primary links from sequence imagemap page
    $self->{geneHrefFn} = $args->{geneHrefFn} if (defined($args->{geneHrefFn}));
    $self->{geneHrefTarget} = $args->{geneHrefTarget} if (defined($args->{geneHrefTarget}));
    $self->{alignHrefFn} = $args->{alignHrefFn} if (defined($args->{alignHrefFn}));
    $self->{alignHrefTarget} = $args->{alignHrefTarget} if (defined($args->{alignHrefTarget}));
    $self->{featureHrefFn} = $args->{featureHrefFn} if (defined($args->{featureHrefFn}));
    $self->{featureHrefTarget} = $args->{featureHrefTarget} if (defined($args->{featureHrefTarget}));
    # secondary links from sequence imagemap page
    $self->{genbankHrefFn} = $args->{genbankHrefFn} if (defined($args->{genbankHrefFn}));
    $self->{genbankHrefTarget} = $args->{genbankHrefTarget} if (defined($args->{genbankHrefTarget}));
    $self->{genpeptHrefFn} = $args->{genpeptHrefFn} if (defined($args->{genpeptHrefFn}));
    $self->{genpeptHrefTarget} = $args->{genpeptHrefTarget} if (defined($args->{genpeptHrefTarget}));
    $self->{sequenceHrefFn} = $args->{sequenceHrefFn} if (defined($args->{sequenceHrefFn}));
    $self->{sequenceHrefTarget} = $args->{sequenceHrefTarget} if (defined($args->{sequenceHrefTarget}));
    $self->{arrayElementHrefFn} = $args->{arrayElementHrefFn} if (defined($args->{arrayElementHrefFn}));
    $self->{arrayElementHrefTarget} = $args->{arrayElementHrefTarget} if (defined($args->{arrayElementHrefTarget}));
    $self->{showDotsRnas} = $args->{showDotsRnas} if (defined($args->{showDotsRnas}));
    $self->{dotsHrefFn} = $args->{dotsHrefFn} if (defined($args->{dotsHrefFn}));
    $self->{dotsHrefTarget} = $args->{dotsHrefTarget} if (defined($args->{dotsHrefTarget}));

    $self->{seqLen} = $args->{seqLen} if (defined($args->{seqLen}));
    $self->{seqLabel} = $args->{seqLabel} if (defined($args->{seqLabel}));
    $self->{highlightGeneId} = $args->{highlightGeneId} if (defined($args->{highlightGeneId}));
    $self->{cgiArgs} = $args->{cgiArgs} if (defined($args->{cgiArgs}));
    $self->{userTracks} = $args->{userTracks} if (defined($args->{userTracks}));

    if (defined($organism)) {
	$self->{organism} = $organism;
    } else {
	$self->getSeqOrganism();
    }

    # Hashref used to track how long it takes to run each query to build the display
    #
    $self->{timing} = {};

    # To avoid calling getRootSpanAndCanvas more than once
    #
    $self->{rootSpanCache} = {};

    return $self;
}

sub startTimer {
    my($self, $name) = @_;
    my $naSeqId = $self->{naSeqId};
    my $timer = {};
    my $time = time;
    $timer->{start} = $time;
    $self->{timing}->{$name} = $timer;
    print STDERR "GUSGenomicSeqTransducer: $naSeqId start $name=$time\n" if ($SHOW_TIMING);
}

sub stopTimer {
    my($self, $name) = @_;
    my $naSeqId = $self->{naSeqId};
    my $timer = $self->{timing}->{$name};
    $timer->{stop} = time;
    my $start = $timer->{start};
    my $stop = $timer->{stop};
    my $elapsed = ($stop - $start);
    $timer->{elapsed} = $elapsed;
    print STDERR "GUSGenomicSeqTransducer: $naSeqId stop $name=$stop elapsed=$elapsed\n" if ($SHOW_TIMING);
    return $elapsed;
} 

sub printTimers {
    my($self) = @_;
    my $naSeqId = $self->{naSeqId};
    my $timers = $self->{timing};

    print STDERR "GUSGenomicSeqTransducer: $naSeqId ";
    foreach my $key (keys %$timers) {
	my $e = $timers->{$key}->{elapsed};
       	print STDERR "$key:$e ";
    }
    print STDERR "\n";
}

sub isPFalciparum {
    my($self) = @_;
    return ($self->{organism} =~ /plasmodium falciparum/i);
}

sub isPYoelii {
    my($self) = @_;
    return ($self->{organism} =~ /plasmodium yoelii/i);
}

sub isPlasmodium {
    my($self) = @_;
    return ($self->{organism} =~ /plasmodium \S+/i);
}

sub getSeqOrganism {
    my($self) = @_;

    my $coreDb = $self->{coreDb};
    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
 
    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getSeqOrganism::starting...",
                                 -1, "\t\t") if $debug;
   
    my $sth = $dbh->prepare("select tn.name as scientific_name " .
			    "from ${dotsDb}.NASequenceImp s, ${sresDb}.TaxonName tn " .
			    "where na_sequence_id = $naSeqId " .
			    "and s.taxon_id = tn.taxon_id (+) " .
			    "and ((tn.name_class = 'scientific_name') or (tn.name_class is null)) ");
    $sth->execute();
    my($name) = $sth->fetchrow_array();
    $sth->finish();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getSeqOrganism::end.",
                              $lastcall, "\t\t") if $debug;

    $self->{organism} = $name;
}

sub getHtml {
    my($self, $imgURL, $x1, $x2, $showGeneList, $prevHtml, $nextHtml, $keyHtml, $scrollbarUrl, $browserUrl) = @_;
    $nextHtml = '&nbsp;' if (!defined($nextHtml));
    $prevHtml = '&nbsp;' if (!defined($prevHtml));

    my $seqLen = $self->{seqLen};
    my $name = $self->{name};
    my $html = '';

    my $imageMap = $self->getImageMap($x1, $x2);
    my $w = $self->{width};
    my $h = $self->{height};

    $html = "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" ALIGN=\"center\">\n";

    $html .= "<TR><TD COLSPAN=\"3\" ALIGN=\"center\">\n";
    $html .= $imageMap;

    $html .= "<IMG SRC=\"${imgURL}\" BORDER=\"1\" WIDTH=\"$w\" HEIGHT=\"$h\" ";
    $html .= "USEMAP=\"#${name}\">";
    $html .= "</TD></TR>\n";

    my $seqLabel = $self->{seqLabel};
    $html .= "<TR><TD COLSPAN=\"3\" ALIGN=\"center\"><FONT FACE=\"arial,helvetica\"><B>$seqLabel</B> [ $x1 - $x2 ]</FONT></TD></TR>";

    if ($self->{useScrollbar}) {
	$html .= "<TR><TD COLSPAN=\"3\" ALIGN=\"center\">";

	# Send IMAGEMAP request to the browser, let it sort things out:
	#
#	$html .= "<A HREF=\"$browserUrl\">";
	$html .= "<IMG ALIGN=\"center\" ISMAP BORDER=\"0\" SRC=\"";
	$html .= "$scrollbarUrl?width=$w&height=30&axis_x1=1&axis_x2=$seqLen&x1=$x1&x2=$x2\"";
	$html .= " WIDTH=\"$w\" HEIGHT=\"30\">";
#	$html .= "</A>";
	$html .= "</TD></TR>\n";
    }

    $html .= "<TR><TD ALIGN=\"left\">$prevHtml</TD>";
    $html .= "<TD ALIGN=\"center\">&nbsp;</TD>";
    $html .= "<TD ALIGN=\"right\">$nextHtml</TD></TR>\n";

    $html .= $keyHtml;

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TR><TD COLSPAN=\"3\">\n";

	$html .= "<FORM NAME=\"${name}_form\">\n";
	$html .= "<TABLE BORDER=\"0\" ALIGN=\"center\">\n";
	$html .= "<TR>";

	$html .= "<TD COLSPAN=\"5\">";
#	$html .= "<INPUT TYPE=\"text\" NAME=\"${name}_defline\" SIZE=\"75\"";
#	$html .= " VALUE=\"Place the mouse over an object to see its description here.\">\n";

	$html .= "<TEXTAREA READONLY NAME=\"${name}_defline\" ROWS=\"4\" COLS=\"80\">";
	$html .= "Place the mouse over an object to see its description here\n";
	$html .= "</TEXTAREA>\n";

	$html .= "<BR CLEAR=\"both\">\n";
	$html .= "</TD>\n";

	$html .= "</TR>";
	$html .= "</TABLE>\n";
	$html .= "</FORM>\n";

	$html .= "</TD></TR>\n";
    }

    $html .= "</TABLE>\n";

    # Include list of genes in the region
    # 
    if ($showGeneList) {
	if ($self->{lastHtml}) {
	    $html .= "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"2\" ";
	    $html .= "BGCOLOR=\"#ccccff\" ALIGN=\"center\">\n";
	    $html .= "<TR><TD>\n";

	    $html .= "<TABLE BORDER=\"0\" CELLPADDING=\"2\" CELLSPACING=\"1\" ";
	    $html .= "WIDTH=\"100%\" BGCOLOR=\"#ffffff\">\n";
	    $html .= $self->{lastHtml} . "\n";
	    $html .= "</TABLE>\n";

	    $html .= "</TD></TR>\n";
	    $html .= "</TABLE>\n";
	}
    }

    return $html;
}

sub getImageMap {
    my($self, $x1, $x2) = @_;
    my $name = $self->{name};

    my ($rs, $canvas) = $self->getRootSpanAndCanvas($x1, $x2);

    my $im .= "<MAP NAME=\"$name\">\n";
    $im .= $rs->makeImageMap($canvas);
    $im .= "</MAP>\n";

    return $im;
}

sub getPng {
    my($self, $x1, $x2) = @_;
    my ($rs, $canvas) = $self->getRootSpanAndCanvas($x1, $x2);
    my $img = $self->{canvas}->getImage();
    return $img->can('png') ? $img->png() : $img->gif();
}

sub getJpeg {
    my($self, $x1, $x2, $quality) = @_;
    my ($rs, $canvas) = $self->getRootSpanAndCanvas($x1, $x2);
    my $img = $canvas->getImage();
    if (!$img->can('jpeg')) { return $self->getPng(); }

    if (defined($quality)) {
	return $img->jpeg($quality);
    }
    return $img->jpeg();
}

my $cp0 = &WDK::Model::GDUtil::Packer::constantPacker(0);

# Return a WDK::Model::GDUtil::Canvas with the desired region rendered on it.
#
sub getRootSpanAndCanvas {
    my($self, $x1, $x2) = @_;

    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};

    my $cache = $self->{rootSpanCache};
    my $cachekey = "$x1:$x2";
    my $cacheval = $cache->{$cachekey};
    my $mapName = $self->{name};
    return @$cacheval if ($cacheval);

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getRootSpanAndCanvas::starting...",
                                  -1, "\t") if $debug;

    $self->startTimer('getRootSpan');

    # Display preferences: currently hard-coded by organism
    #
    my $showRnas = 0;
    my $showSAGETags = 0;
    my $showBlastHits = 0;
    my $showSelfBlastHits = 0;
    my $showMarkers = 0;
    my $showRESites = 0;
    my $showSeqPairs = 0;
    my $showOligos = 0;

    my $sp1 = WDK::Model::GDUtil::Packer::simplePacker(2);
    my $lrp1 = WDK::Model::GDUtil::Packer::leftToRightPacker(2);

    my $canvas =  
      WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
			    {x1 => 150, x2 => $self->{width} - 25},
			    {x1 => $x1, x2 => $x2 });

    $canvas->allocateWebSafePalette();
#    print STDERR "GUSGenomicSeqTransducer: allocated ", $canvas->getImage->colorsTotal(), " color(s)\n";

    my $grey = $canvas->getImage()->colorExact(204,204,204);
    my $black = $canvas->getImage()->colorExact(0,0,0);
    my $lblue = $canvas->getImage()->colorExact(153,153,255);

#    print STDERR "GUSGenomicSeqTransducer: allocated ", $canvas->getImage->colorsTotal(), " color(s)\n";

    # Gene predictions grouped by algorithm and strand
    #
    my $gSpans = $self->getGenePredictionSpans($x1, $x2, $canvas);

    my $topLevelSpans = [];
    my @predTypes = keys %$gSpans;

    if ($self->isPlasmodium()) {
	$showBlastHits = 1;
    }

    my $userTracks = $self->{userTracks};

    foreach my $ut (@$userTracks) {
	my $userSpans = $self->getUserSpans($x1, $x2, $canvas, $ut);
	push(@$topLevelSpans, $self->makeStripeSpan($userSpans, $ut->{'name'}, $sp1));
    }

    if ($self->isPFalciparum()) {

	$showRnas = 1;
	$showSAGETags = 1;
	$showSeqPairs = 1;
	$showOligos = 1;
	$showBlastHits = 1;
	$showSelfBlastHits = 1;
	$showMarkers = 1;
	$showRESites = 1;

	my $chr = $self->{chromosome};

	# Tandem repeats from TandemRepeatFeature
	#
	my($show) = $self->getTrackParams('trf');
	my($p1) = getPackers($show);

	if ($show ne 'none') {
	    my $tr = $self->getTandemRepeatSpans($x1, $x2, undef, $canvas);
	    push(@$topLevelSpans, $self->makeStripeSpan($tr, "Tandem repeats", $p1));
	    push(@$topLevelSpans, $self->makeSpacer());
	}

	push(@predTypes, 'FullPhat') if (!grep(/FullPhat/, @predTypes));
	push(@predTypes, 'Genefinder') if (!grep(/Genefinder/, @predTypes));
	push(@predTypes, 'GlimmerM') if (!grep(/GlimmerM/, @predTypes));
	
	# There's a single track in PlasmoDB 4.0 for all the sequencing center predictions
	#
	push(@predTypes, 'Pf Annotation') if (!grep(/pf annotation/i, @predTypes));
    } 

    foreach my $alg (sort { $b cmp $a } @predTypes) {
	my $fwdSpans = $gSpans->{$alg}->{fwd};
	my $revSpans = $gSpans->{$alg}->{rev};

	push(@$topLevelSpans, $self->makeStripeSpan($revSpans, "$alg (-)"));
	push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	push(@$topLevelSpans, $self->makeStripeSpan($fwdSpans, "$alg (+)"));
	push(@$topLevelSpans, $self->makeSpacer());
    }

    if ($showRnas) {
	my($show) = $self->getTrackParams('pf-est');
	my($p1, $p2) = getPackers($show);

	# EST BLAT alignments
	#
	if ($show ne 'none') {
	    my $estSpans = $self->getESTAlignmentSpans($x1, $x2, $canvas);
	    push(@$topLevelSpans, $self->makeStripeSpan($estSpans->{rev}, "Pf EST/GSS (-)", $p2));
	    push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	    push(@$topLevelSpans, $self->makeStripeSpan($estSpans->{fwd}, "Pf EST/GSS (+)", $p1));
	}
    }

    # BLAT-aligned gene predictions from previous versions of PlasmoDB
    #
    my($gpShow) = $self->getTrackParams('3.3-genes');
    my($gpp1, $gpp2) = getPackers($gpShow);

    if ($gpShow =~ /all|one/) {
	    my $geneSpans = $self->getGeneAlignmentSpans($x1, $x2, $canvas);
	    push(@$topLevelSpans, $self->makeStripeSpan($geneSpans->{rev}, "v3.3 genes (-)", $gpp2));
	    push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	    push(@$topLevelSpans, $self->makeStripeSpan($geneSpans->{fwd}, "v3.3 genes (+)", $gpp1));
    }

    # Hexamer predictions stored in HexamerFeature
    #
    my($hshow) = $self->getTrackParams('hexamer');
    my($hp1, $hp2) = getPackers($hshow);
    
    if ($hshow ne 'none') {
	my $hs = $self->getHexamerSpans($x1, $x2, $canvas);
	
	push(@$topLevelSpans, $self->makeStripeSpan($hs->{rev}, "Hexamer (-)", $hp2));
	push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	push(@$topLevelSpans, $self->makeStripeSpan($hs->{fwd}, "Hexamer (+)", $hp1));
	push(@$topLevelSpans, $self->makeSpacer());
    }

    # Low complexity sequence
    #
    my $lcSpans = $self->getLowComplexitySpans($x1, $x2, $canvas);
    push(@$topLevelSpans, $self->makeStripeSpan($lcSpans, "low complexity seq.", $cp0));

    # all similarity results
    #
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $ssbuilder = WDK::Model::GDUtil::Transducer::GUSSimilarityQueryBuilder->new(
        {dbh => $dbh, coreDb => $coreDb, dotsDb => $dotsDb, naSeqId => $naSeqId, start => $x1, end => $x2});

    # Non-falciparum searches against P. falciparum
    #
    my $props = {};    
    if ($self->isPFalciparum()) {
        # Show tblastx searches against non-falciparum Plasmodium draft sequence
        # (currently knowlesi and chabaudi)
        #
        $self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'na', 0, 'none', '-genomic');

        # Same thing for EST/GSS sequences
        #
        $self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'na', 0, 'only', '-est');
    } 

    # P. falciparum searches against non-falciparum
    #
    elsif ($self->isPlasmodium()) {
        $self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'na', 0, undef, 'fnf');
    }

    if ($showOligos) {
	# Winzeler Chr 2. Affy oligos: external_database_release_id = 5493, maxPValExp = 1
	#
	$self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'na', 1, undef, 'olg1', 1, 5493);

	# DeRisi oligos: external_database_release_id = 15, maxPValExp = 1
	#
	$self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'na', 1, undef, 'olg2', 1, 15);
    }

    # BLAST hits (genomic sequence vs. NRDB)
    #
    $self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'aa') if $showBlastHits;

    # BLAST hits (genomic sequence vs. genomic sequence)
    #
    $self->buildSimQueryAndSaveDisplayProps($props, $ssbuilder, 'na', undef, undef, 'slf') 
        if ($showSelfBlastHits); 

    # get all similarity spans
    #
    my $simTracks = $self->getSimilarityTrackHash($props, $ssbuilder, $canvas);

    # add similarity spans in desired order
    #
    
    # Non-falciparum searches against P. falciparum
    #
    if ($self->isPFalciparum()) {
        foreach my $pfs (@$PLASMODIUM_SPECIES) {
	    my $firstID = $pfs->{taxon_ids}->[0];
            next if ($firstID == 211);
            if ($pfs->{have_genomic}) {
                my $trackName = $pfs->{display_name};
                $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, $trackName, $topLevelSpans, $canvas, $ssbuilder);
            }
        }
        # Same thing for EST/GSS sequences
	#
        foreach my $pfs (@$PLASMODIUM_SPECIES) {
	    my $firstID = $pfs->{taxon_ids}->[0];
            next if ($firstID == 211);
            if ($pfs->{have_est_gss}) {
                my $trackName = $pfs->{display_name} . " EST/GSS";
                $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, $trackName, $topLevelSpans, $canvas, $ssbuilder);
            }
        }
    }

    # P. falciparum searches against non-falciparum
    #
    elsif ($self->isPlasmodium()) {
        foreach my $pfs (@$PLASMODIUM_SPECIES) {
	    my $firstID = $pfs->{taxon_ids}->[0];
            next if ($firstID == 211);
            if ($pfs->{have_genomic}) {
                my $trackName = $pfs->{display_name};
                $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, $trackName, $topLevelSpans, $canvas, $ssbuilder);
            }
        }
    }

    if ($self->{showDotsRnas}) {
	my ($rSpans, $html) = $self->getTranscriptSpans($x1, $x2, $canvas, 1);
	push(@$topLevelSpans, $self->makeStripeSpan($rSpans->{rev}, "DoTS RNAs (-)", $sp1));
	push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	push(@$topLevelSpans, $self->makeStripeSpan($rSpans->{fwd}, "DoTS RNAs (+)", $sp1));
	push(@$topLevelSpans, $self->makeSpacer());
    }

    # my $nshow) = $self->getTrackParams('nmrc');
    my($mbshow) = $self->getTrackParams('mung-bean');
    my($p1) = getPackers($mbshow);

    if ($mbshow ne 'none') {
	if ($showSeqPairs) {
	    my $eSpans = $self->getEPCRSpans($x1, $x2, $canvas);
	    
	    # NMRC primers: external_database_release_id = 3793
	    #
	    #	my $nmrc = $eSpans->{3793};
	    #	push(@$topLevelSpans, $self->makeStripeSpan($nmrc, "NMRC primers", $lrp1, 1));
	    #	push(@$topLevelSpans, $self->makeSpacer());
	    
	    # Mung bean paired end reads: external_database_release_id = 3994
	    my $mb = $eSpans->{3994};
	    push(@$topLevelSpans, $self->makeStripeSpan($mb, "Mung bean ends", $p1, 1));
	    push(@$topLevelSpans, $self->makeSpacer());
	}
    }

    if ($showOligos) {
        # Winzeler Affy oligos: external_database_release_id = 4092
        #
        $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, "Winzeler chr2 oligos", $topLevelSpans, $canvas, $ssbuilder);

        # derisi-oligos
        #
        $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, "DeRisi 70-mers", $topLevelSpans, $canvas, $ssbuilder);
    }

    if ($showSAGETags) {
	my($show) = $self->getTrackParams('sage');
	my($p1, $p2) = getPackers($show);

	if ($show ne 'none') {
	    my ($rSpans, $html) = $self->getSAGETagSpans($x1, $x2, $canvas);
	    push(@$topLevelSpans, $self->makeStripeSpan($rSpans->{rev}, "SAGE tags (-)", $p2));
	    push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	    push(@$topLevelSpans, $self->makeStripeSpan($rSpans->{fwd}, "SAGE tags (+)", $p1));
	    push(@$topLevelSpans, $self->makeSpacer());
	}
    }

    # BLAST hits (genomic sequence vs. NRDB)
    #
    if ($showBlastHits) {
        $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, "BLASTX", $topLevelSpans, $canvas, $ssbuilder);
    }

    # BLAST hits (genomic sequence vs. genomic sequence)
    #
    if ($showSelfBlastHits) {
        $topLevelSpans = $self->addSimilaritySpanTrack($props, $simTracks, "Self-BLASTN", $topLevelSpans, $canvas, $ssbuilder);
    }

    # Microsatellite markers
    #
    my($mshow) = $self->getTrackParams('microsat');
    if ($showMarkers && $self->isPFalciparum() && ($mshow ne 'none')) {
	my ($mSpans) = $self->getMarkerSpans($x1, $x2, $canvas);
	push(@$topLevelSpans, $self->makeStripeSpan($mSpans, "Microsatellites", $cp0));
	push(@$topLevelSpans, $self->makeSpacer());
    }

    # Restriction enzyme sites
    #
    my($rshow) = $self->getTrackParams('resites');
    if ($showRESites && ($rshow ne 'none')) {
	my $reSpans = $self->getRestrictionSiteSpans($x1, $x2, $canvas);
	push(@$topLevelSpans, $self->makeStripeSpan($reSpans, "Restriction sites", $cp0));
	push(@$topLevelSpans, $self->makeSpacer());
    }

    # Percent AT plot, window size = 100bp
    # Only display percent AT plot if sequence < 200kb ?
    #
    my $seqLen = $x2 - $x1;

    if ($seqLen <= 200000) {
	my $args = {
	    x1 => $x1, x2 => $x2, 
	    naseq => $self->getSequence($x1, $x2), 
	    height => 30, label => '%AT plot (W=100)', windowSize => 100,
	    imagemapLabel => "Percent-AT plot with window size = 100bp",
	    imagemapHref => '',
	    lineColor => $lblue,
	};

	my $descr = &safeHtml("Percent-AT plot with window size = 100bp");
	
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $at100 = WDK::Model::GDUtil::PercentATSpan->new($args);
	push(@$topLevelSpans, $at100);
    }
    push(@$topLevelSpans, $self->makeSpacer());

    # Centromeres and telomeres
    #
    my $cenTelSpans = $self->getCentromereTelomereSpans($x1, $x2, $canvas);
    push(@$topLevelSpans, $self->makeSpacer());
    push(@$topLevelSpans, $self->makeStripeSpan($cenTelSpans, "telomere/centromere"));

    # Add ScaffoldGapFeatureSpans last
    #
    my $sgfs = $self->getScaffoldGapFeatureSpans($x1, $x2, $canvas);
    push(@$topLevelSpans, @$sgfs);

    # Tick interval
    #
    my $ti = 10000;
    $ti = 'ends' if ($self->{seqLen} == $x2);
    my $slbl = defined($self->{seqLabel}) ? 
	$self->{seqLabel} : "NASequence " . $self->{naSeqId};

    my $rootSpan = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => $x1, 
	  x2 => $x2, 
	  y1 => $self->{height} - 5,
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => $topLevelSpans,
	  packer => WDK::Model::GDUtil::Packer::simplePacker(4),
	  tickInterval => $ti,
	  tickLabel => 'bp',
	  label => $slbl,
	  labelVAlign => 'bottom'
      });
    
    $self->startTimer('pack1');
    $rootSpan->pack();
    $self->stopTimer('pack1');

    # Ugly, but the only way
    #
    if ($self->{autoheight}) {
	$self->{height} = $rootSpan->getHeight() + 5;

	$canvas = 
	  WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
				{x1 => 150, x2 => $self->{width} - 25},
				{x1 => $x1, x2 => $x2});

	$canvas->allocateWebSafePalette();
	$self->{canvas} = $canvas;
	$rootSpan->{y1} = $self->{height} - 5;

	$self->startTimer('pack2');
	$rootSpan->pack();
	$self->stopTimer('pack2');
    }

    $self->startTimer('draw');
    $rootSpan->draw($canvas);
    $self->stopTimer('draw');

    $self->stopTimer('getRootSpan');
#    $self->printTimers();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getRootSpanAndCanvas::end...",
                               $lastcall, "\t") if $debug;

    my @result = ($rootSpan, $canvas);
    $cache->{$cachekey} = \@result;
    return @result;
}

sub getTrackParams {
    my($self, $trackname, $dfltPVal, $dfltPctId, $dfltLen) = @_;
    my $track = $self->{cgiArgs}->{$trackname};

    my $show = $self->{cgiArgs}->{$trackname . "_track"};

    my $pval = $self->{cgiArgs}->{$trackname . "_pval"};
    $pval = $dfltPVal if ($pval =~ /^\s*$/);
    $pval = undef if ($pval =~ /none/i);

    my $pctid = $self->{cgiArgs}->{$trackname . "_pctid"};
    $pctid = $dfltPctId if ($pctid =~ /^\s*$/);
    $pctid = undef if ($pctid =~ /none/i);

    my $len = $self->{cgiArgs}->{$trackname . "_len"};
    $len = $dfltLen if ($len =~ /^\s*$/);
    $len = undef if ($len =~ /none/i);

    return ($show, $pval, $pctid, $len);
}

sub makeStripeSpan {
    my($self, $spans, $caption, $packer, $sort) = @_;

    my @sorted;

    if (!defined($spans)) {
	@sorted = ();
    } elsif ($sort) {
#	print STDERR "GUSGenomicSeqTransducer: sorting spans on sortScore\n";
	@sorted = sort { $b->{sortScore} <=> $a->{sortScore} } @$spans;
#	foreach my $s (@sorted) {
#	    print STDERR "GUSGenomicSeqTransducer: score=", $s->{sortScore}, " descr=", $s->{imagemapLabel}, "\n";
#	}
    } else {
	@sorted = @$spans;
    }

    return WDK::Model::GDUtil::StripeSpan->new({kids => \@sorted,
				    packer => defined($packer) ? $packer : $cp0, 
				    label => $caption,
				    labelVAlign => 'center',
#				    drawBar => 1
				    });
}

sub makeSpacer {
    my($self) = @_;
    return WDK::Model::GDUtil::Span->new({height => 1,
			      shape => 'none'
			      });
}

# Get ESTs aligned to genomic sequence using BLAT
#
sub getESTAlignmentSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};

    my $img = $canvas->getImage();
    my $estColor = $img->colorExact(204,102,153);
    my $gssColor = $img->colorExact(153,102,204);

    my $exonHeight = 10;
    my $intronOffset = -4;

    my $sql = ("select /*+ INDEX(ba, BLATAlignment_IND04) */ ena.source_id, st.name, ena.length, ba.percent_identity, " .
	       "ba.is_reversed, ba.number_of_spans, ba.blocksizes, ba.qstarts, ba.tstarts " .
	       "from ${dotsDb}.BLATAlignment ba, ${dotsDb}.ExternalNASequence ena, ${dotsDb}.SequenceType st " .
	       "where ba.target_na_sequence_id = $naSeqId " .
	       "and ba.blat_alignment_quality_id = 1 " .
	       "and ba.query_na_sequence_id = ena.na_sequence_id " .
	       "and ba.target_start <= $end " .
	       "and ba.target_end >= $start " .
	       "and ena.sequence_type_id = st.sequence_type_id ");

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getESTAlignmentSpans::\nsql=$sql ....",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($src, $type, $len, $pct, $isrev, $nb, $bs, $qs, $ts) = $sth->fetchrow_array()) {
	my @bSizes = split(',', $bs);
	my @tStarts = split(',', $ts);
	my $exons = [];

	my $geneX1 = undef;
	my $geneX2 = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;

	my $color = ($type =~ /EST/i) ? $estColor : $gssColor;

	# Process blocks/exons + introns
	#
	for (my $i = 0;$i < $nb; ++$i) {
	    my $size = $bSizes[$i];
	    my $tstart = $tStarts[$i];

	    my $x1 = $tstart + 1;
	    my $x2 = $tstart + $size;

	    next if (($x2 < $start) || ($x1 > $end));

	    # Truncate exon to image boundaries
	    #
	    $x1 = $start if ($x1 < $start);
	    $x2 = $end if ($x2 > $end);

	    # Draw introns
	    #
	    if (defined($lastX1)) {
		if ($lastX2 < $x1) {
		    $lastX2 = $start if ($lastX2 < $start);
		    push(@$exons, &makeIntron($lastX2, $x1, $color, $intronOffset));
		} else {
		    push(@$exons, &makeIntron($x2, $lastX1, $color, $intronOffset));
		}
	    }

	    push(@$exons, &makeExon($x1, $x2, $color, 0, $exonHeight));

	    if (not defined($geneX1)) {
		$geneX1 = $x1; $geneX2 = $x2;
	    } else {
		$geneX1 = $x1 if ($x1 < $geneX1);
		$geneX2 = $x2 if ($x2 > $geneX2);
	    }
	    $lastX1 = $x1; $lastX2 = $x2;
	}

	my $descr = &safeHtml("$src: $len bp $type aligned by BLAT at $pct% identity with $nb span(s)");

	my $args = {
	    x1 => $geneX1,
	    x2 => $geneX2,
	    kids => $exons,
	    shape => 'none',
	    height => 0,
	    imagemapLabel => $descr,
	    packer => $cp0
	};
			
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	# TO DO - provide link to detailed BLAT alignment

	my $align = WDK::Model::GDUtil::Span->new($args);

	if ($isrev) {
	    push(@$revSpans, $align);
	} else {
	    push(@$fwdSpans, $align);
	}
    }

    $sth->finish();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getESTAlignmentSpans::end.",
                              $lastcall, "\t\t") if $debug;

    return {fwd => $fwdSpans, rev => $revSpans};
}

# Get gene predictions from previous versions of PlasmoDB aligned to genomic sequence using BLAT
#
sub getGeneAlignmentSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};

    my $img = $canvas->getImage();
    my $color = $img->colorExact(255,102,102);

    my $exonHeight = 10;
    my $intronOffset = -4;

    my $sql = ("select /*+ INDEX(ba, BLATAlignment_IND04) */ gf.product, gf.source_id, ba.percent_identity, " .
	       "ba.is_reversed, ba.number_of_spans, ba.blocksizes, ba.qstarts, ba.tstarts " .
	       "from ${dotsDb}.BLATAlignment ba, ${dotsDb}.RNAFeature rnaf, ${dotsDb}.GeneFeature gf " .
	       "where ba.target_na_sequence_id = $naSeqId " .
	       "and ba.blat_alignment_quality_id = 1 " .
	       "and ba.query_na_sequence_id = rnaf.na_sequence_id " .
	       "and rnaf.parent_id = gf.na_feature_id " .
	       "and ba.query_table_id = 339 " .                            # SplicedNASequence
	       "and ba.target_start <= $end " .
	       "and ba.target_end >= $start ");

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGeneAlignmentSpans::\nsql=$sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($src, $id, $pct, $isrev, $nb, $bs, $qs, $ts) = $sth->fetchrow_array()) {
	my @bSizes = split(',', $bs);
	my @tStarts = split(',', $ts);
	my $exons = [];

	my $geneX1 = undef;
	my $geneX2 = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;

	# Process blocks/exons + introns
	#
	for (my $i = 0;$i < $nb; ++$i) {
	    my $size = $bSizes[$i];
	    my $tstart = $tStarts[$i];

	    my $x1 = $tstart + 1;
	    my $x2 = $tstart + $size;

	    next if (($x2 < $start) || ($x1 > $end));

	    # Truncate exon to image boundaries
	    #
	    $x1 = $start if ($x1 < $start);
	    $x2 = $end if ($x2 > $end);

	    # Draw introns
	    #
	    if (defined($lastX1)) {
		if ($lastX2 < $x1) {
		    $lastX2 = $start if ($lastX2 < $start);
		    push(@$exons, &makeIntron($lastX2, $x1, $color, $intronOffset));
		} else {
		    push(@$exons, &makeIntron($x2, $lastX1, $color, $intronOffset));
		}
	    }

	    push(@$exons, &makeExon($x1, $x2, $color, 0, $exonHeight));

	    if (not defined($geneX1)) {
		$geneX1 = $x1; $geneX2 = $x2;
	    } else {
		$geneX1 = $x1 if ($x1 < $geneX1);
		$geneX2 = $x2 if ($x2 > $geneX2);
	    }
	    $lastX1 = $x1; $lastX2 = $x2;
	}

	my $descr = &safeHtml("PlasmoDB 3.3|$id: aligned by BLAT at $pct% identity with $nb span(s)");

	my $args = {
	    x1 => $geneX1,
	    x2 => $geneX2,
	    kids => $exons,
	    shape => 'none',
	    height => 0,
	    imagemapLabel => $descr,
	    packer => $cp0
	};
			
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	# TO DO - provide link to detailed BLAT alignment

	my $align = WDK::Model::GDUtil::Span->new($args);

	if ($isrev) {
	    push(@$revSpans, $align);
	} else {
	    push(@$fwdSpans, $align);
	}
    }

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGeneAlignmentSpans::end.",
                              $lastcall, "\t\t") if $debug;

    $sth->finish();
    return {fwd => $fwdSpans, rev => $revSpans};
}

sub getLowComplexitySpans {
    my($self, $start, $end, $canvas) = @_;

    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};

    my $spans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};

    my $img = $canvas->getImage();
    my $color = $img->colorExact(153,204,153);

    # TO DO - dots.LowComplexityNAFeature is missing

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getLowComplexitySpans::starting...",
                                 -1, "\t\t") if $debug;

    my $sql = ("select nal.start_min, nal.end_max, a.name " .
	       "from LowComplexityNAFeature lc, ${dotsDb}.NALocation nal, ${coreDb}.Algorithm a " .
	       "where lc.na_sequence_id = $naSeqId " .
	       "and lc.na_feature_id = nal.na_feature_id " . 
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "and lc.prediction_algorithm_id = a.algorithm_id " . 
	       "order by nal.start_min asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my ($x1, $x2, $name) = $sth->fetchrow_array()) { 
	my $descr = "low complexity sequence ($x1 - $x2) predicted by $name";

	$x1 = $start if ($x1 < $start);
	$x2 = $end if ($x2 > $end);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $descr,
	    imagemapTarget => '',
	    imagemapHref => '#',
	};

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }
    $sth->finish();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getLowComplexitySpans::end.",
                              $lastcall, "\t\t") if $debug;

    return $spans;
}

# Hexamer is a predictor of coding potential
#
sub getHexamerSpans {
    my($self, $start, $end, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};

    my $color1 = $img->colorExact(102,102,102);
    my $color2 = $img->colorExact(153,153,153);
    my $color3 = $img->colorExact(204,204,204);

    my $sql = ("select hf.score, nal.start_min, nal.end_max, nal.is_reversed " .
	       "from ${dotsDb}.HexamerFeature hf, ${dotsDb}.NALocation nal " .
	       "where hf.na_sequence_id = $naSeqId " .
	       "and hf.na_feature_id = nal.na_feature_id " . 
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "and hf.score >= 25 ");
    
    $self->startTimer('hexamer');

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getHexamerSpans::\nsql=$sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getHexamerSpans::sql executed...",
                              $lastcall, "\t\t") if $debug;

    while (my ($score, $x1, $x2, $isrev) = $sth->fetchrow_array()) { 
	my $color;

	if ($score < 100) {
	    $color = $color3;
	} elsif ($score < 200) {
	    $color = $color2;
	} else {
	    $color = $color1;
	}

	my $descr = "hexamer prediction ($x1 - $x2) with score = $score";

	$x1 = $start if ($x1 < $start);
	$x2 = $end if ($x2 > $end);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $descr,
	    imagemapTarget => '',
	    imagemapHref => '#',
	};

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	if ($isrev) {
	    push(@$revSpans, WDK::Model::GDUtil::Span->new($args));
	} else {
	    push(@$fwdSpans, WDK::Model::GDUtil::Span->new($args));
	}
    }
    $sth->finish();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getHexamerSpans::end.",
                              $lastcall, "\t\t") if $debug;

    $self->stopTimer('hexamer');

    return {fwd => $fwdSpans, rev => $revSpans};
}

# Generate spans representing large-scale chromosomal features.
#
sub getCentromereTelomereSpans {
    my($self, $start, $end, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{geneHrefFn};
    my $hrefTarget = $self->{geneHrefTarget};
    my $mapName = $self->{name};

    my $color = $canvas->getImage()->colorExact(51,51,204);
    my $spans = [];

    # Centromeres
    #
    my $sql = ("select nal.start_min, nal.end_max, cf.name, cf.length, cf.percent_at " .
	       "from ${dotsDb}.ChromosomeElementFeature cf, ${dotsDb}.NAlocation nal " .
	       "where cf.na_feature_id = nal.na_feature_id " .
	       "and cf.na_sequence_id = $naSeqId " .
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    while(my($x1, $x2, $name, $len, $at) = $sth->fetchrow_array()) {
	$x2 = $end if ($x2 > $end);
	$x1 = $start if ($x1 < $start);
	my $centromere = ($name =~ /centromere/i);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => $centromere ? 12 : 10,
	    color => $color,
	    filled => 1,
	};

	my $descr = $centromere ? "$name length=$len percent_AT=${at}%" : "$name length=$len";
	$descr = &safeHtml($descr);
		
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }
    $sth->finish();
    return $spans;
}

# Generate vertical lines on top of the image to denote gaps in the sequence.
#
sub getScaffoldGapFeatureSpans {
    my($self, $start, $end, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $color = $img->colorExact(255,255,255);

# not bad:
#    my $fgColor = $img->colorExact(150,150,150);

    my $fgColor = $img->colorExact(0,0,255);
    my $spans = [];

    # TO DO - dots.ScaffoldGapFeature doesn't exist yet

    my $sql = ("select nal.start_min, nal.end_max " .
	       "from ScaffoldGapFeature sgf, ${dotsDb}.NALocation nal " .
	       "where sgf.na_sequence_id = $naSeqId " .
	       "and sgf.na_feature_id = nal.na_feature_id " .
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "order by nal.start_min asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($x1, $x2) = $sth->fetchrow_array()) {
	push(@$spans, WDK::Model::GDUtil::VertLineSpan->new({
	    x1 => $x1,
	    x2 => $x2,
	    color => $color,
	    fgColor => $fgColor,
	    width => 3,
	}));
    }

    $sth->finish();

    return $spans;
}

# User-defined spans
#
sub getUserSpans {
    my($self, $start, $end, $canvas, $userTrack) = @_;
    my $intervals = $userTrack->{intervals};
    my $spans = [];

    foreach my $intvl (@$intervals) {
	my $x1 = $intvl->{x1};
	my $x2 = $intvl->{x2};
	my $col = $intvl->{color};
	my $isrev = ($x1 > $x2);

	my $img = $canvas->getImage();
	my $fgColor = $img->colorExact($col->[0], $col->[1], $col->[2]);

	if ($isrev) {
	    push(@$spans, &makeExon($x2, $x1, $fgColor, 1));
	} else {
	    push(@$spans, &makeExon($x1, $x2, $fgColor, 1));
	}
    }

    return $spans;
}

# Tandem repeats predicted by Gary Benson's Tandem Repeat Finder
#
sub getTandemRepeatSpans {
    my($self, $start, $end, $minLen, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $spans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};
    my $mapName = $self->{name};

    my $color1 = $img->colorExact(51,51,255);
    my $color2 = $img->colorExact(153,153,255);
    my $color3 = $img->colorExact(204,204,255);

    my $sql = ("select trf.period, trf.copynum, trf.score, trf.entropy, trf.consensus, " .
	       "nal.start_min, nal.end_max " .
	       "from ${dotsDb}.TandemRepeatFeature trf, ${dotsDb}.NALocation nal " .
	       "where trf.na_sequence_id = $naSeqId " .
	       "and trf.na_feature_id = nal.na_feature_id " . 
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       (defined($minLen) ? "and (nal.end_max - nal.start_min) >= $minLen " : ""));
    
    $self->startTimer('trf');

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTandemRepeatSpans::\nsql = $sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTandemRepeatSpans::sql executed ...",
                              $lastcall, "\t\t") if $debug;

    while (my ($period, $copynum, $score, $entropy, $consensus, $x1, $x2) = $sth->fetchrow_array()) { 
	my $color;
	my $len = $x2 - $x1;

	if ($score < 50) {
	    $color = $color3;
	} elsif ($score < 100) {
	    $color = $color2;
	} else {
	    $color = $color1;
	}

	my $descr = "tandem repeat ($x1-$x2) length=$len score=$score period=$period copynum=$copynum consensus=$consensus";

	$x1 = $start if ($x1 < $start);
	$x2 = $end if ($x2 > $end);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $descr,
	    imagemapTarget => '',
	    imagemapHref => '#',
	};

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }
    $sth->finish();
 
    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTandemRepeatSpans::end",
                              $lastcall, "\t\t") if $debug;

    $self->stopTimer('trf');
    return $spans;
}

# Get the sequence for this region of the DNA
#
sub getSequence {
    my($self, $sx1, $sx2) = @_;
    my $dotsDb = $self->{dotsDb};
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};

    # Need to convert sequence browser coordinates to 1-based for sequence retrieval
    #
    my $x1 = $sx1 + 1;
    my $x2 = $sx2 + 1;
    my $len = $x2 - $x1 + 1;
    my $seq = '';
    
    my $query = "select DBMS_LOB.SUBSTR(sequence,?,?) from ${dotsDb}.NASequenceImp where na_sequence_id = $naSeqId";
    my $stmt = $dbh->prepare($query);

    for(my $s = $x1; $s < $x1 + $len;$s += 4000){
	$stmt->execute(($s + 4000 <= $x1 + $len) ? 4000 : $len + $x1 - $s,$s);
	while(my($str) = $stmt->fetchrow_array()){
	    $seq .= $str;
	}
    }
    return $seq;
}

# Return all gene predictions that overlap a specified region.
#
sub getGenePredictionSpans {
    my($self, $start, $end, $canvas) = @_;

    my $coreDb = $self->{coreDb};
    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();

    my $fgColor = $canvas->getDefaultFgColor();
    my $annotatedColor = $img->colorExact(255,153,153);
    my $dfltColor = $img->colorExact(102,102,153);

    # Render the automated gene predictions in a lighter shade of grey
    # to focus attention on the official annotations.
    #
    my $glimmerMColor = $img->colorExact(153,153,153);
    my $fullPhatColor = $img->colorExact(153,153,153);
    my $genefinderColor = $img->colorExact(153,153,153);

    my $highlightColor = $img->colorExact(255,0,0);

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{geneHrefFn};
    my $hrefTarget = $self->{geneHrefTarget};
    my $mapName = $self->{name};

    # For "official" sequencing center/centre annotation
    #
    my $annotator = undef;
    my $chr = $self->{'chromosome'};

#    print STDERR "GUSGenomicSeqTransducer: organism = ", $self->{organism}, " chromosome = ", $self->{chromosome}, "\n";

    my $geneRows = [];

    my $sql = ("select distinct gf.na_feature_id, gf.name, gf.source_id, gf.product, " .
	       "ed.name, a.name, gf.gene_type, nal.start_min, nal.end_max, " .
	       "nal.is_reversed, gf.number_of_exons " .
	       "from ${dotsDb}.GeneFeature gf, ${dotsDb}.NALocation nal, " .
	       "${sresDb}.ExternalDatabaseRelease edr, ${sresDb}.ExternalDatabase ed, ${coreDb}.Algorithm a " .
	       "where gf.na_sequence_id = $naSeqId " .
	       "and gf.na_feature_id = nal.na_feature_id " .
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "and a.algorithm_id (+) = gf.prediction_algorithm_id " .
	       "and edr.external_database_release_id (+) = gf.external_database_release_id " .
	       "and edr.external_database_id = ed.external_database_id (+) " .
	       "order by nal.start_min, a.name ");

    $self->startTimer('genes');
    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGenePredictionSpans::\nsql = $sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGenePredictionSpans::sql executed ...",
                              $lastcall, "\t\t") if $debug;
   
    while (my @row = $sth->fetchrow_array()) { push(@$geneRows, \@row); }
    $sth->finish();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGenePredictionSpans::looping throu",
                              $lastcall, "\t\t") if $debug;

    # Hashref indexed by GeneFeature.na_feature_id
    #
    my $geneExonHash = {};
    # Query for coordinates of all the exons of the above genes
    #
    my $exonSql = ("select distinct ef.order_number, el.start_min, el.start_max, " . 
                   "el.end_min, el.end_max, gf.na_feature_id ".
		   "from ${dotsDb}.GeneFeature gf, ${dotsDb}.NALocation gl, " .
		   "${dotsDb}.ExonFeature ef, ${dotsDb}.NALocation el " .
		   "where gf.na_sequence_id = $naSeqId " .
		   "and gf.na_feature_id = gl.na_feature_id " .
		   "and gl.start_min <= $end " .
		   "and gl.end_max >= $start " .
		   "and ef.parent_id = gf.na_feature_id " .
		   "and el.na_feature_id = ef.na_feature_id " .
		   "order by gf.na_feature_id, el.start_min asc ");

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGenePredictionSpans::\nsub sql = $exonSql ...",
			      $lastcall, "\t\t\t") if $debug;

    my $sth = $dbh->prepare($exonSql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGenePredictionSpans::executed",
			      $lastcall, "\t\t\t") if $debug;

    while (my @row = $sth->fetchrow_array()) {
        if ($geneExonHash->{$row[5]}) {
            push (@{ $geneExonHash->{$row[5]} }, \@row);
        } else {
            $geneExonHash->{$row[5]} = [\@row];
        } 
    }
    $sth->finish();

    # Gene predictions indexed by prediction method (Algorithm.name)
    # and then by strand.
    #
    my $genePredSpans = {};

    my $genesFwd = [];
    my $genesRev = [];

    foreach my $row (@$geneRows) {
	my $naFeatId = $row->[0];
	my $name = $row->[1];
	my $sourceId = $row->[2];
	my $product = $row->[3];
	my $dbname = $row->[4];
	my $alg = $row->[5];
	my $type = $row->[6];
	my $startMin = $row->[7];
	my $endMax = $row->[8];
	my $isRev = $row->[9];
	my $numExons = $row->[10];

	$alg = 'unknown' if ($alg =~ /^\s*$/);

	my $exonHeight = 8;
	my $intronOffset = -3;

	if ($dbname =~ /Plasmodium_.*Sanger/i) {
	    $annotator = 'Sanger';
	} elsif ($dbname =~ /Plasmodium_.*TIGR/i) {
	    $annotator = 'TIGR';
	} elsif ($dbname =~ /Plasmodium_.*Stanford/i) {
	    $annotator = 'Stanford';
	} else {
	    $annotator = undef;
	}

	my $color;
	my $intronColor;
	my $exonBorder = 0;

	if ($naFeatId == $self->{geneNaFeatId}) {
	    $color = $highlightColor;
	    $intronColor = $fgColor;
	    $exonBorder = 1;
	} elsif (defined($annotator)) {
	    $color = $annotatedColor;
	    $intronColor = $fgColor;
	    $exonBorder = 1;
	} elsif ($alg =~ /glimmerm/i) {
	    $intronColor = $color = $glimmerMColor;
	} elsif ($alg =~ /fullphat/i) {
	    $intronColor = $color = $fullPhatColor;
	} elsif ($alg =~ /genefinder/i) {
	    $intronColor = $color = $genefinderColor;
	} else {
	    $intronColor = $color = $dfltColor;
	}

	if (defined($annotator)) {
	    $exonHeight = 10;
	    $intronOffset = -4;
	}

	my $exons = [];
	my $fcolor = "#000000";

	my $geneX1 = undef;
	my $geneX2 = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;

        my $rows = $geneExonHash->{$naFeatId};
        foreach my $row (@$rows) {
	    my ($x1, $x2);
	    if ($row->[1] < $row->[4]) {
		$x1 = $row->[1];
		$x2 = $row->[4];
	    } else {
		$x1 = $row->[4];
		$x2 = $row->[1];
	    }

	    # Truncate/remove exons and introns at boundaries
	    #
	    if ($x2 < $start) {
		$lastX1 = $x1; $lastX2 = $x2;
		next;
	    }
	    last if ($x1 > $end);
	    $x1 = $start if ($x1 < $start);
	    $x2 = $end if ($x2 > $end);

	    # Draw introns
	    #
	    if (defined($lastX1)) {
		if ($lastX2 < $x1) {
		    $lastX2 = $start if ($lastX2 < $start);
		    push(@$exons, &makeIntron($lastX2, $x1, $intronColor, $intronOffset));
		} else {
		    push(@$exons, &makeIntron($x2, $lastX1, $intronColor, $intronOffset));
		}
	    }

	    push(@$exons, &makeExon($x1, $x2, $color, $exonBorder, $exonHeight));

	    if (not defined($geneX1)) {
		$geneX1 = $x1; $geneX2 = $x2;
	    } else {
		$geneX1 = $x1 if ($x1 < $geneX1);
		$geneX2 = $x2 if ($x2 > $geneX2);
	    }
	    $lastX1 = $x1; $lastX2 = $x2;
	}

	my $descr;

	if (defined($annotator)) {
	    $descr = "${sourceId}: ${product} [$type gene annotated by $annotator]";
	} else {
	    $descr = "${sourceId}: ${product} [$type gene predicted by $alg]";
	}
	$descr = &safeHtml($descr);

	my $gArgs = {
	    x1 => $geneX1,
	    x2 => $geneX2,
	    kids => $exons,
	    shape => 'none',
	    height => 0,
	    imagemapLabel => $descr,
	    packer => $cp0,
	};
		
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $gArgs->{imagemapOnMouseOver} = $mOver;
	}

	$gArgs->{imagemapHref} = &$hrefFn($sourceId) if (defined($hrefFn));
	$gArgs->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));

	my $gene = WDK::Model::GDUtil::Span->new($gArgs);

	# Group by official annotator or gene prediction method
	#
	my $groupKey = $alg;
	my $algSpans = $genePredSpans->{$groupKey};
	my($fwd, $rev);

	if (!defined($algSpans)) {
	    $fwd = []; $rev = [];
	    $genePredSpans->{$groupKey} = {fwd => $fwd, rev => $rev};
	} else {
	    $fwd = $algSpans->{fwd};
	    $rev = $algSpans->{rev};
	}

	if ($isRev) {
	    push(@$rev, $gene);
	} else {
	    push(@$fwd, $gene);
	}
    }
 
    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getGenePredictionSpans::end",
                              $lastcall, "\t\t") if $debug;
   
    $self->stopTimer('genes');
    return $genePredSpans;
}

# Display spans from the EPCR/EndSequencePairMap tables.  Returns a 
# hashref indexed by external_database_release_id
#
sub getEPCRSpans {
    my($self, $start, $end, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $dbHash = {};
    
    my $img = $canvas->getImage();
    my $fgColor = $canvas->getDefaultFgColor();

    my $black = $img->colorExact(153,153,153);

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};
    my $mapName = $self->{name};

    my $sql = ("select espm.external_database_release_id, espm.source_id, epcr.start_pos, epcr.stop_pos, " .
	       "       epcr.num_mismatches, epcr.is_reversed " .
	       "from ${dotsDb}.EndSequencePairMap espm, ${dotsDb}.EPCR epcr " .
	       "where epcr.na_sequence_id = $naSeqId " .
	       "and epcr.start_pos <= $end " .
	       "and epcr.stop_pos >= $start " .
	       "and epcr.map_id = espm.end_sequence_pair_map_id " .
	       "order by espm.external_database_release_id, epcr.start_pos, epcr.stop_pos ");

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getEPCRSpans::\nsql = $sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getEPCRSpans::sql executed ...",
                              $lastcall, "\t\t") if $debug;

    while (my($edb, $srcId, $x1, $x2, $nmm, $isRev) = $sth->fetchrow_array()) { 

	$x1 = $start if ($x1 < $start);
	$x2 = $end if ($x2 > $end);
	
	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $black,
	    imagemapLabel => $srcId,
	    imagemapHref => "#",
	    imagemapTarget => $hrefTarget,
	};
	my $descr = &safeHtml($srcId) . " [$x1 - $x2] num_mismatches=$nmm";

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	my $span = WDK::Model::GDUtil::Span->new($args);

	# Add span to appropriate list
	#
	my $list = $dbHash->{$edb};

	if (defined($list)) {
	    push(@$list, $span);
	} else {
	    $dbHash->{$edb} = [$span];
	}
    }

    $sth->finish();


    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getEPCRSpans::end ...",
                              $lastcall, "\t\t") if $debug;

    return $dbHash;
}

# Display SAGE tags with number of occurrences/genome
#
sub getSAGETagSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};
    my $radDb = $self->{radDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
    my $fgColor = $canvas->getDefaultFgColor();

    my $tagColor = $img->colorExact(153,153,153);

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};
    my $mapName = $self->{name};

    my $sql = ("select gss.spot_family_id, sfr.experiment_result_id, sfr.default_value, " .
	       "       stf.name, nal_tag.start_min, nal_tag.end_max, nal_tag.is_reversed " .
	       "from ${dotsDb}.SAGETagFeature stf, ${dotsDb}.NALocation nal_tag, " .
	       "     ${radDb}.GenomicSAGESpot gss, ${radDb}.SpotFamilyResult sfr " .
	       "where stf.na_sequence_id = $naSeqId " .
	       "and stf.tag_location_id = nal_tag.location_id " .
	       "and nal_tag.start_min <= $end " .
	       "and nal_tag.start_max >= $start " .
	       "and stf.na_feature_id = gss.tag_feature_id " .
	       "and gss.spot_family_id = sfr.spot_family_id ");
#	       "group by stf.name, nal_tag.start_min, nal_tag.end_max, nal_tag.is_reversed ");
    
#    print STDERR "GUSGenomicSeqTransducer: running '$sql'\n";

    $self->startTimer('sage');

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getSAGETagSpans::\nsql = $sql ...",
                                  -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);

    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getEPCRSpans::sql executed...",
                              $lastcall, "\t\t") if $debug;

    my $tagRows = [];

    while (my($spotFamId, $expResultId, $tagCount, $name, $x1, $x2, $isrev) = $sth->fetchrow_array()) { 
        my $desc = "SAGE tag $name (count = $tagCount)";
	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $tagColor,
	    filled => 1,
	    imagemapLabel => $desc,
	    imagemapTarget => '',
	    imagemapHref => '#',
	};
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$desc';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$desc';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	if ($isrev) {
	    push(@$revSpans, WDK::Model::GDUtil::Span->new($args));
	} else {
	    push(@$fwdSpans, WDK::Model::GDUtil::Span->new($args));
	}
    }
    $sth->finish();
    $self->stopTimer('sage');

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getEPCRSpans::end",
                              $lastcall, "\t\t") if $debug;

    return {fwd => $fwdSpans, rev => $revSpans};
}

# mRNA/EST/GSS sequences BLAST-sim4 aligned with the genomic sequence
#
sub getTranscriptSpans {
    my($self, $start, $end, $canvas, $dotsRnas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
#    my $fgColor = $canvas->getDefaultFgColor();
    my $fgColor = $img->colorExact(153,102,102);

    my $estColor = $img->colorExact(255,102,102);
    my $gssColor = $img->colorExact(0,0,0);
    my $highlightColor = $img->colorExact(255,0,0);
    my $highlightId = $self->{'highlightNASeqId'};

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};

    my($hrefFn, $hrefTarget);

     if ($dotsRnas) {
	$hrefFn = $self->{dotsHrefFn};
	$hrefTarget = $self->{dotsHrefTarget};
    } else {
	$hrefFn = $self->{alignHrefFn};
	$hrefTarget = $self->{alignHrefTarget};
    }

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTranscriptSpans::starting...",
                                 -1, "\t\t") if $debug;

    my $sql = "select " . (($dotsRnas) ? 
		"('DT.' || seq.na_sequence_id) as source_id, seq.description, seq.sequence_type_id, seq.length, "
		:"seq.source_id, seq.secondary_identifier, seq.sequence_type_id, seq.length, ") .
	       "ss.subject_start, ss.subject_end, ss.query_start, ss.query_end, " .
	       "ss.is_reversed, ss.number_identical, ss.match_length, " .
	       "ca.avg_identity, ca.dots_bases_aligned, ca.number_of_spans, s.similarity_id " .
	       "from ${dotsDb}.Similarity s, " .
	       (($dotsRnas) ? "${dotsDb}.Assembly seq, " : "${dotsDb}.ExternalNASequence seq, " ) .
	       "     ${dotsDb}.SimilaritySpan ss, ${dotsDb}.ConsistentAlignment ca " .
	       "where ca.genomic_na_sequence_id = $naSeqId " .
	       (($self->{showInconsistentAlignments}) ? "" : "and ca.is_consistent = 1 ") .
	       "and ca.transcript_na_sequence_id = seq.na_sequence_id " .
	       ($dotsRnas ? "" : "and seq.external_database_release_id = 6519 ") .
	       "and ca.similarity_id = s.similarity_id " .
	       "and s.similarity_id = ss.similarity_id " .
	       "and ss.subject_start <= $end " .
	       "and ss.subject_end >= $start " .
	       "order by " . ($dotsRnas ? "seq.na_sequence_id" : "seq.source_id") . ", ss.subject_start ";

#    print STDERR "GUSGenomicSeqTransducer: running '$sql'\n";

    $self->startTimer('transcripts');

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTranscriptSpans::\nsql = $sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);

    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTranscriptionSpans::sql executed ...",
                              $lastcall, "\t\t") if $debug;

    my $simRows = [];

    while (my @row = $sth->fetchrow_array()) { 
	push(@$simRows, \@row); 
    }
    $sth->finish();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTranscriptSpans::end.",
                              $lastcall, "\t\t") if $debug;

    # Group rows by source ID (column 0)
    #
    my $groups = &groupRows($simRows, 0);

    foreach my $gp (keys %$groups) {
	my $rows = $groups->{$gp};
	my $kidSpans = [];
	my $min = undef;
	my $max = undef;
	my $reversed = undef;
	my $descr = undef;
	my $sourceId = undef;
        my $simId = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;

	foreach my $row (@$rows) {
	    my($srcId, $secId, $seqType, $len, $ss, $se, $qs, $qe, $isrev, $nid, $ml, $avgId, $alignBp, $ns, $sim) = @$row;
	    my $color = $fgColor;

	    if ($seqType == 8) {
		$color = $estColor;
	    } elsif ($seqType == 21) {
		$color = $gssColor;
	    }

#	    print STDERR "GUSGenomicSeqTransducer: secondary_identifier='$secId' source_id='$srcId'\n";

	    if (!defined($min)) {
		$min = $ss;
		$max = $se;
		$reversed = $isrev;

		if ($dotsRnas) {
		    $descr = "$srcId: $secId";
		} else {
		    $descr = "$srcId: $alignBp/$len bp, ${avgId}% avg. identity, $ns apparent exon(s)";
		}

		$sourceId = $srcId;
                $simId = $sim;
		$lastX1 = $ss;
		$lastX2 = $se;
	    } else {
		$min = $ss if ($ss < $min);
		$max = $se if ($se > $max);
		
		push(@$kidSpans, &makeIntron($lastX2, $ss, $fgColor));
	    }

	    push(@$kidSpans, &makeExon($ss, $se, $color, 1));
	    $lastX1 = $ss; $lastX2 = $se;
	}

	my $args = {
	    x1 => $min,
	    x2 => $max,
	    kids => $kidSpans,
	    shape => 'none',
	    height => 0,
	    packer => $cp0,
	    imagemapLabel => $descr,
	    imagemapHref => "#",
	    imagemapTarget => $hrefTarget,
	};
	$args->{imagemapHref} = &$hrefFn($simId) if (defined($hrefFn));
	$descr = &safeHtml($descr);

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $aSpan = WDK::Model::GDUtil::Span->new($args);
	if ($reversed) { push(@$revSpans, $aSpan); } else { push(@$fwdSpans, $aSpan); }
    }

    $self->stopTimer('transcripts');

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getTranscriptionSpans::end",
                              $lastcall, "\t\t") if $debug;

    return {fwd => $fwdSpans, rev => $revSpans};
}

# Add all BLAST similarities spans that overlap a specified region to top level spans
#
sub getSimilarityTrackHash {
    my($self, $props, $ssbuilder, $canvas) = @_;

    my $rs = $ssbuilder->getSimilaritySpanResultSet;

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer::getSimilarityTrackHash::looping through ResultSet",
                                 -1, "\t") if $debug;
    my $tracks = {};
    my ($trackName, $trackProps, $trackRows);
    while (my @row = $rs->fetchrow_array()) {
        my $taxId = shift @row;
        my $seqTid = shift @row;
        my $extDid = shift @row;
        if ($taxId == 211 && $seqTid == 121) {
            $trackProps = $props->{"211:$extDid"};
        } elsif ($taxId > 0 && $taxId != 211 && ($seqTid == 8 || $seqTid == 21)) {
            $trackProps = $props->{"$taxId:$seqTid"};
        } else {
            $trackProps = $props->{"$taxId"};
        }
        # use display name to group rows into specific tracks
        $trackName = $trackProps->{displayName};
	my $minPctId = $trackProps->{minPctId};
	my $minLen = $trackProps->{minLen};

	# Only include this row (span) in the result set if it meets
	# the minimum (span) length and percent identity cutoffs.
	#
	my($srcId,$descr,$ss,$se,$qs,$qe,$isrev,$score,
	   $mant,$exp,$nid, $ml,$snid,$sml,$s3,$s4,$s5,$simId) = @row;
	my $pctId = ($ml == 0) ? 0 : int(($nid / $ml) * 100.0);

	my $lenMet = ($minLen =~ /\d+/) ? ($ml >= $minLen) : 1;
	my $idMet = ($minPctId =~ /\d+/) ? ($pctId >= $minPctId) : 1;

	if ($lenMet && $idMet) {
	    if ($tracks->{$trackName}) {
		$trackRows = $tracks->{$trackName}->{simRows};
		push(@$trackRows, \@row);
	    } else {
		$tracks->{$trackName} = { trackProps => $trackProps, simRows => [\@row] };
	    }
	}
    }
    $rs->finish();
    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer::getSimilarityTrackHash::done",
                              $lastcall, "\t") if $debug;
    return $tracks;
}

sub addSimilaritySpanTrack {
    my ($self, $props, $tracks, $tn, $topLevelSpans, $canvas, $ssbuilder) = @_;
    my $grey = $canvas->getImage()->colorExact(204,204,204);

    my $trackProps = $tracks->{$tn}->{trackProps};
    $trackProps = getPropsByDisplayName($props, $tn);
    my $trackRows = $tracks->{$tn}->{simRows};

    my $dn = $trackProps->{displayName};
    my($show) = $trackProps->{show};

#    print STDERR "GUSGenomicSeqTransducer: trackProps=$trackProps dn='$dn' $tn.show =  '$show'\n";
    if (!defined($dn)) { print STDERR "GUSGenomicSeqTransducer: trackProps.keys() = ", join(",", keys %$trackProps), "\n"; }
    
    if ($show ne 'none') {
	my($p1, $p2) = getPackers($show);

	my $ss = $self->getSimilaritySpans($canvas, $trackRows, $trackProps, $ssbuilder);
#DEBUG
#foreach my $span (@{ $ss->{rev} }) {
#print STDERR "DEBUG: Sim Rev Span: " .
#             "(x1=$span->{x1}, x2=$span->{x2}, y1=$span->{y1}, imagemapLabel=$span->{imagemapLabel})\n";
#}
	push(@$topLevelSpans, $self->makeStripeSpan($ss->{rev}, "$tn (-)", $p2, 1));
	push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	push(@$topLevelSpans, $self->makeStripeSpan($ss->{fwd}, "$tn (+)", $p1, 1));
	push(@$topLevelSpans, $self->makeSpacer());
    }
 
    return $topLevelSpans;
}

# Rerurn spans for given similarity rows
#
sub getSimilaritySpans {
    my($self, $canvas, $simRows, $props, $ssbuilder) = @_;

    my $hrefFn = $props->{hrefFn};
    my $hrefTarget = $props->{hrefTarget};
    my $hrefFn2 = $props->{hrefFn2};
    my $hrefTarget2 = $props->{hrefTarget2};
    my $linkQuery = $props->{linkQuery};
    my $start = $ssbuilder->{start};
    my $end = $ssbuilder->{end};

    my $showIntrons = $props->{showIntrons};
    my $summary = $props->{summary};    

    # HACK
    $summary = 1;

    my $img = $canvas->getImage();
#    my $fgColor = $canvas->getDefaultFgColor();
    my $fgColor = $img->colorExact(153,102,102);
    my $ltGrey = $img->colorExact(204,204,204);
    my $mapName = $self->{name};

    my $fwdSpans = [];
    my $revSpans = [];

    # Group rows by source ID (column 0)
    #
    my $groups = &groupRows($simRows, 0);

    foreach my $gp (keys %$groups) {
	my $rows = $groups->{$gp};
	my $kidSpans = [];
	my $min = undef;
	my $max = undef;
	my $reversed = undef;
	my $label = undef;
	my $sourceId = undef;
	my $similarityId = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;
	my $lastQX1 = undef;	
	my $lastQX2 = undef;

	my($sPctId, $sscore, $sSrcId, $sDescr, $smant, $sexp);

	foreach my $row (@$rows) {
	    my($srcId,$descr,$ss,$se,$qs,$qe,$isrev,$score,
               $mant,$exp,$nid, $ml,$snid,$sml,$s3,$s4,$s5,$simId) = @$row;
	    my $pctId = int(($nid / $ml) * 100.0);

	    if (!defined($sPctId)) {
		my $sPctId = ($sml == 0) ? 0 : int(($snid / $sml) * 100.0);
		$sscore = $s3;
		$smant = $s4;
		$sexp = $s5;
		$sSrcId = $srcId;
		$sDescr = $descr;
	    }

	    # Needed to handle "reverse" queries
	    #
	    if ($ss > $se) {
		my $tmp = $ss;
		$ss = $se;
		$se = $tmp;
		$isrev = 1;
	    }

	    # Truncate/remove exons and introns at boundaries
	    #
	    if ($se < $start) {
		$lastX1 = $ss; $lastX2 = $se;
		next;
	    }
	    last if ($ss > $end);
	    $ss = $start if ($ss < $start);
	    $se = $end if ($se > $end);

	    my $pval = sprintf("%fe%f", $mant, $exp);
	    my($color, $fill) = &WDK::Model::GDUtil::Transducer::GUSSimilarityTransducer::getHspColorAndFill($canvas, $pval);

	    if (!defined($min)) {
		$min = $ss;
		$max = $se;
		$reversed = $isrev;
		
		$similarityId = $simId;
		$sourceId = $srcId;
		$lastX1 = $ss;
		$lastX2 = $se;
		$lastQX1 = $qs;
		$lastQX2 = $qe;
	    } else {
		$min = $ss if ($ss < $min);
		$max = $se if ($se > $max);
		
		if ($showIntrons && (abs($qs - $lastQX2) <= 4)) {
		    if ($lastX2 < $ss + 20) {
			$lastX2 = $start if ($lastX2 < $start);
#			print STDERR "GUSGEnomicSeqTransducer: $srcId intron 1 $lastX2 - $ss\n";
			push(@$kidSpans, &makeIntron($lastX2, $ss, $fgColor));
		    } else {
#			print STDERR "GUSGEnomicSeqTransducer: $srcId intron 2 $se - $lastX1\n";
			push(@$kidSpans, &makeIntron($se, $lastX1, $fgColor));
		    }
		}
	    }

#	    print STDERR "GUSGEnomicSeqTransducer: $srcId exon $ss - $se\n";

	    push(@$kidSpans, &makeExon($ss, $se, $color, 0));
	    $lastX1 = $ss; $lastX2 = $se;
	}

	my $sortScore;

	if ($sPctId > 0) {
	    my $slbl = "$sSrcId: ${sPctId}% identity";
	    $slbl .= " to $sDescr" if (defined($sDescr));
	    $label = &safeHtml($slbl);
	} elsif (defined($smant) && ($smant >= 0)) {
	    $sexp = 0 if ($sexp <= -999999);
	    my $sPval = sprintf("%fe%f", $smant, $sexp);
	    my $slbl = sprintf("$sSrcId: p-value=%3.0e score=$sscore ", $sPval);
	    $slbl .= $sDescr if (defined($sDescr));
	    $label = &safeHtml($slbl);
	} elsif ($sscore > 0) {
	    my $slbl = "$sSrcId: score = $sscore ";
	    $slbl .= $sDescr if (defined($sDescr));
	    $label = &safeHtml($slbl);
	} else {
	    $label = &safeHtml("$sSrcId");
	}

	if ($sscore > 0) {
	    $sortScore = $sscore;
	}

	my $args = {
	    x1 => $min,
	    x2 => $max,
	    kids => $kidSpans, # $summary ? [] : $kidSpans,
	    shape => $summary ? 'rect' : 'none',
	    color => $ltGrey,
	    filled => 0,
	    height => $summary ? 1 : 0,
	    packer => $cp0,
	    imagemapLabel => $label,
	    imagemapHref => "#",
	    imagemapTarget => $hrefTarget,
	};

	$args->{imagemapHref} = &$hrefFn($similarityId) if defined($hrefFn);
        $args->{imagemapHref} .= "&url=" . &WDK::Model::GDUtil::Transducer::MyEscape::uri_escape(&$hrefFn2($sourceId)) if $hrefFn2;
        $args->{imagemapHref} .= "&target=$hrefTarget2" if $hrefTarget2;
        $args->{imagemapHref} .= "&linkQuery=1" if $linkQuery;

	$label = &safeHtml($label);

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$label';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$label';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $span = WDK::Model::GDUtil::Span->new($args);
	$span->{sortScore} = $sortScore;

	if ($reversed) { push(@$revSpans, $span); } else { push(@$fwdSpans, $span); }
    }

    return {fwd => $fwdSpans, rev => $revSpans};
}

# Plasmodium (currently) microsatellite markers mapped to the sequence
#
sub getMarkerSpans {
    my($self, $start, $end, $canvas, $showIntrons) = @_;
    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};
    my $mapName = $self->{name};

    my $color = $img->colorExact(0,0,255);

    my $sql = ("select e.start_pos, e.stop_pos, e.num_mismatches, pm.chromosome, pm.centimorgans, pm.marker_name, " .
	       "       pm.accession, pm.framework, pm.cross " .
	       "from ${dotsDb}.plasmomap pm, ${dotsDb}.epcr e " .
	       "where e.na_sequence_id = $naSeqId " .
	       "and e.start_pos <= $end " .
	       "and e.stop_pos >= $start " .
	       "and e.map_table_id = 471 " .
	       "and e.map_id = pm.plasmomap_id ");

    my $mRows = [];

    $self->startTimer('markers');

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getMarkerSpans::\nsql = $sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getMarkerSpans::sql executed ...",
                              $lastcall, "\t\t") if $debug;

    while (my @row = $sth->fetchrow_array()) { push(@$mRows, \@row); }
    $sth->finish();

    my $markerSpans = [];

    foreach my $row (@$mRows) {
	my($x1, $x2, $nmm, $chrom, $cm, $name, $accn, $fw, $cross) = @$row;
	my $lbl = &safeHtml("Marker $name locn=$x1-$x2 num_mismatches=$nmm acc=$accn map=chr.${chrom}, ${cm}cM $fw cross=$cross");

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $lbl,
	};

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$lbl';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$lbl';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	push(@$markerSpans, WDK::Model::GDUtil::Span->new($args));
    }
    $self->stopTimer('markers');

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getMarkerSpans::end",
                              $lastcall, "\t\t") if $debug;

    return $markerSpans;
}

sub getRestrictionSiteSpans {
    my($self, $start, $end, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $spans = [];

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};
    my $img = $canvas->getImage();
    my $color = $img->colorExact(0,0,255);
    my $mapName = $self->{name};

    my $sql = ("select distinct rff.enzyme_name, nal.start_min, nal.start_max " .
	       "from ${dotsDb}.RestrictionFragmentFeature rff, ${dotsDb}.NALocation nal " .
	       "where rff.na_sequence_id = $naSeqId " .
	       "and rff.na_feature_id = nal.na_feature_id " .
	       "and nal.start_min <= $end " .
	       "and nal.start_max >= $start " .
	       "order by nal.start_min, nal.start_max ");

    $self->startTimer('reSites');

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getRestrictionSiteSpans::\nsql = $sql ...",
                                 -1, "\t\t") if $debug;

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSGenomicSeqTransducer.pm::getRestrictionSiteSpans::sql executed ...",
                              $lastcall, "\t\t") if $debug;

    while (my($en, $x1, $x2) = $sth->fetchrow_array()) { 
        next if $x1 == 1 and $x2 == 1;
        my $desc = &safeHtml("$en restriction enzyme site");
	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $desc,
	};
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$desc';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$desc';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }

    $sth->finish();
    $self->stopTimer('reSites');
    return $spans;
}

sub getTableId {
    my($self, $tname) = @_;
    my $coreDb = $self->{coreDb};

    my $dbh = $self->{dbh};
    my $tid;

    my $sql = "select table_id from ${coreDb}.TableInfo where table_name = '$tname'";
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    if (my @row = $sth->fetchrow_array()) {
	$tid = $row[0];
    }
    $sth->finish();
    
    return $tid;
}

sub buildSimQueryAndSaveDisplayProps {
    my ($self, $props, $ssbuilder, $queryType, $seqIsSubject, $estAndGss, $tagSuffix, $maxPval, $extDbId) = @_;
    if ($tagSuffix =~ /olg2/i) {
        my ($show) = $self->getTrackParams('derisi-oligos');
        $props->{"211:" . $extDbId} = { displayName => "DeRisi 70-mers", show => $show, 
                showIntrons => 0, summary => 0,
                hrefFn => $self->{alignHrefFn}, hrefTarget => $self->{alignHrefTarget},
                hrefFn2 => $self->{arrayElementHrefFn}, hrefTarget2 => $self->{arrayElementHrefTarget} };
        $ssbuilder->buildNAQuery($maxPval, $seqIsSubject, $estAndGss, undef, $extDbId);
        return;
    }
    if ($tagSuffix =~ /olg1/i) {
        my ($show) = $self->getTrackParams('winzeler-oligos');
        $props->{"211:" . $extDbId} = { displayName => "Winzeler chr2 oligos", show => $show, 
                showIntrons => 0, summary => 0,
                hrefFn => $self->{alignHrefFn}, hrefTarget => $self->{alignHrefTarget},
                hrefFn2 => $self->{arrayElementHrefFn}, hrefTarget2 => $self->{arrayElementHrefTarget} };
        $ssbuilder->buildNAQuery($maxPval, $seqIsSubject, $estAndGss, undef, $extDbId);
        return;
    }
    if ($tagSuffix =~ /slf/i) {
        my($show, $maxPval, $minPctId, $minLen) = $self->getTrackParams('self-blastn', -100, undef, undef);
        $props->{"211"} = { displayName => "Self-BLASTN", show => $show, showIntrons => 0,
			    hrefFn => $self->{alignHrefFn}, hrefTarget => $self->{alignHrefTarget},
			    hrefFn2 => $self->{sequenceHrefFn}, hrefTarget2 => $self->{sequenceHrefTarget},
			    maxPval => $maxPval, minPctId => $minPctId, minLen => $minLen};
        $ssbuilder->buildNAQuery($maxPval, undef, undef, [211]);
        return;
    }
    if ($queryType =~ /aa/i) {
        my($show, $maxPval, $minPctId, $minLen) = $self->getTrackParams('nrdb', -100, undef, undef);
        $props->{"-1"} = { displayName => "BLASTX", show => $show, showIntrons => 1,
			   hrefFn => $self->{alignHrefFn}, hrefTarget => $self->{alignHrefTarget},
			   hrefFn2 => $self->{genpeptHrefFn}, hrefTarget2 => $self->{genpeptHrefTarget},
			   maxPval => $maxPval, minPctId => $minPctId, minLen => $minLen};
        $ssbuilder->buildAAQuery($maxPval);
        return;
    }
    foreach my $pfs (@$PLASMODIUM_SPECIES) {
        my $taxonIds = $pfs->{taxon_ids};
        next if ($taxonIds->[0] == 211);
        next if ($tagSuffix =~ /fnf/i && !$pfs->{have_genomic}) 
             or ($tagSuffix eq '-genomic' && !$pfs->{have_genomic})
             or ($tagSuffix eq '-est' && !$pfs->{have_est_gss});
        my $pvalCutoff = $pfs->{pval_cutoff};
        my $name = $pfs->{display_name};
        my $tag = $pfs->{twoletter} ;
        my ($showIntr, $sum, $linkQuery) = (1, 1, 1);
        if ($tagSuffix eq '-genomic') {
            $tag .= '-genomic';
            $showIntr = 0;
        } elsif ($tagSuffix eq '-est') {
            $tag .= '-est';
            $name .= " EST/GSS";
            $sum = 0;
        }

        my($show, $maxPval, $minPctId, $minLen) = $self->getTrackParams($tag, $pvalCutoff, undef, undef);
#	print STDERR "GUSGenomicSeqTransducer: got parameters for '$tag' : show = $show\n";
#	print STDERR "GUSGenomicSeqTransducer: $tag maxPval=$maxPval minPct=$minPctId minLen=$minLen seqIsSubject=$seqIsSubject\n";
       
        my $p = { displayName => $name, show => $show, showIntrons => $showIntr, summary => $sum,
                  hrefFn => $self->{alignHrefFn}, hrefTarget => $self->{alignHrefTarget},
                  hrefFn2 => $self->{sequenceHrefFn}, hrefTarget2 => $self->{sequenceHrefTarget},
		  maxPval => $maxPval, minPctId => $minPctId, minLen => $minLen};
        $p->{linkQuery} = 1 if $linkQuery;
        
        foreach my $taxonId (@$taxonIds) {
            if ($estAndGss =~ /only/i) {
                $props->{"$taxonId:8"} = $props->{"$taxonId:21"} = $p; 
#		print STDERR "GUSGenomicSeqTransducer: setting '$taxonId:8' and '$taxonId:21' - show $show\n";
            } else {
                $props->{$taxonId} = $p;
#		print STDERR "GUSGenomicSeqTransducer: setting '$taxonId' - show $show\n";
            }
        }
        $ssbuilder->buildNAQuery($maxPval, $seqIsSubject, $estAndGss, $taxonIds);
    }
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

# Group a set of rows by a specific column
#
sub groupRows {
    my($rows, $cnum) = @_;
    my $groups = {};

    foreach my $row (@$rows) {
	my $colval = $row->[$cnum];
	my $group = $groups->{$colval};

	if (!defined($group)) {
	    $group = [];
	    $groups->{$colval} = $group;
	}
	push(@$group, $row);
    }

    return $groups;
}

sub getPackers {
    my ($show) = @_;
    my ($p1, $p2);
    if ($show =~ /all/) {
        $p1 = WDK::Model::GDUtil::Packer::linePacker(2, 50);
        $p2 = WDK::Model::GDUtil::Packer::linePacker(2, 50, 1);
    } else {
        $p1 = $p2 = $cp0;
    }
    return ($p1, $p2);
}

sub getPropsByDisplayName {
    my ($props, $dn) = @_;
    foreach my $pk (keys %$props) {
        my $prop = $props->{$pk};
        return $prop if ($prop->{displayName} eq $dn);
    }
}
sub makeExon {
    my($start, $end, $color, $border, $height) = @_;
    
    my $args = {
	x1 => $start,
	x2 => $end,
	height => $height ? $height : 8,
	color => $color,
	filled => 1,
	};

    $args->{border} = 1 if ($border);
    return WDK::Model::GDUtil::Span->new($args);
}

sub makeIntron {
    my($start, $end, $color, $vertoffset) = @_;
    
    my $args = {
	x1 => $start,
	x2 => $end,
	height => 1,
	vertOffset => $vertoffset ? $vertoffset : -3,
	color => $color,
	filled => 1,
	};
    
    return WDK::Model::GDUtil::Span->new($args);
}

