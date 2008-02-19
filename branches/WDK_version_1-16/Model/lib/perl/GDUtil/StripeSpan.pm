#!/usr/bin/perl

#------------------------------------------------------------------------
# StripeSpan.pm
#
# A Span whose horizontal extent is equal to that of the drawing area.  
# That is, it is always as wide as the current display.  It can also
# have a label that appears off the left or right of the main drawing
# area.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::StripeSpan;

use strict;

use GD;
use WDK::Model::GDUtil::Span;
@WDK::Model::GDUtil::StripeSpan::ISA = ('WDK::Model::GDUtil::Span');

#-------------------------------------------------
# Configuration
#-------------------------------------------------

my $DFLT_LBL_HALIGN = 'left';
my $DFLT_LBL_VALIGN = 'center';
my $DFLT_LBL_GAP = 5;
my $DFLT_LBL_FONT = gdSmallFont;

#-------------------------------------------------
# StripeSpan
#-------------------------------------------------

sub new {
    my($class, $span) = @_;
    my $self = $class->SUPER::new($span);

    my $lbl = $span->{label};
    my $halign = $span->{labelHAlign};  # 'left' or 'right'
    my $valign = $span->{labelVAlign};  # 'top', 'bottom', or 'center'
    my $lblg = $span->{labelGap};
    my $lblf = $span->{labelFont};

    $self->{label} = $lbl if defined($lbl);
    $self->{labelHAlign} = defined($halign) ? $halign : $DFLT_LBL_HALIGN;
    $self->{labelVAlign} = defined($valign) ? $valign : $DFLT_LBL_VALIGN;
    $self->{labelGap} = defined($lblg) ? $lblg : $DFLT_LBL_GAP;
    $self->{labelFont} = defined($lblf) ? $lblf : $DFLT_LBL_FONT;
    $self->{drawBar} = 1 if defined($span->{drawBar});
    $self->{imagemapArea} = $span->{imagemapArea} if defined($span->{imagemapArea});

    bless($self, $class);
    return $self;
}

#-------------------------------------------------
# Span
#-------------------------------------------------

sub getHeight {
    my($self) = @_;
    my $sh = &WDK::Model::GDUtil::Span::getHeight($self);
    my $font = $self->{labelFont};
    my $fh = $font->height;
    return ($fh > $sh) ? $fh : $sh;
}

sub getSelfHeight {
    my($self) = @_;
    return 0;
}

sub makeImageMap {
    my($self, $gdCanvas) = @_;
    my $im = '';

    my $shape = $self->{shape};
    my $label = $self->{imagemapLabel};
    my $href = $self->{imagemapHref};
    my $target = $self->{imagemapTarget};
    my $onMouseOver = $self->{imagemapOnMouseOver};
    my $onMouseOut = $self->{imagemapOnMouseOut};
    my $area = $self->{imagemapArea};

    if ($label || $href || $onMouseOver || $onMouseOut) {
	my $sc = $gdCanvas->getScreenCoords();

	my $x1 = (($self->{labelHAlign} eq 'left') ? 0 : $sc->{x2}) + $self->{labelGap};
	my $x2 = ($self->{labelHAlign} eq 'left') ? $sc->{x1} - 1 : $sc->{x2} + 1;

	# Get bounding box, convert coordinates.
	#
	my $bbox = $self->getBBox($gdCanvas);
	my $coords;

	# IMAGEMAP AREA = entire span
	#
	if ($area eq 'all') {
	    $coords = [$0, 
		       $bbox->{y} - $bbox->{height},
		       $sc->{x2},
		       $bbox->{y}
		       ];	    
	} 
	else {
	    $coords = [$x1, 
		       $bbox->{y} - $bbox->{height},
		       $x2,
		       $bbox->{y}
		       ];
	}

	$im .= "  <AREA SHAPE=\"rect\" COORDS=\"" . join(',', @$coords) . "\" ";

	if (defined($href)) {
	    $im .= "HREF=\"$href\" ";
	} else {
	    $im .= "NOHREF ";
	}

	$im .= "TARGET=\"$target\" " if ($target);
	$im .= "TITLE=\"$label\" " if ($label);

	if ($onMouseOver || $onMouseOut) {
	    $im .= "ONMOUSEOVER=\"${onMouseOver}\" " if $onMouseOver;
	    $im .= "ONMOUSEOUT=\"${onMouseOut}\" " if $onMouseOut;
	} 
	elsif ($label) {
	    $im .= "ONMOUSEOVER=\"window.status='$label'; return true;\" ";
	    $im .= "ONMOUSEOUT=\"window.status=''; return true;\" ";
	}

	$im .= ">\n";
    }

    foreach my $kid (@{$self->{kids}}) {
	$im .= $kid->makeImageMap($gdCanvas);
    }

    return $im;
}

sub drawSelf {
    my($self, $gdCanvas) = @_;
    my $lbl = $self->{label};

    if (defined($lbl)) {
	my $sc = $gdCanvas->getScreenCoords();
	my $image = $gdCanvas->getImage();
	my $font = $self->{labelFont};

	my $fh = $font->height;
	my $x = (($self->{labelHAlign} eq 'left') ? 0 : $sc->{x2}) + $self->{labelGap};
	my $y1;
	
	if ($self->{labelVAlign} eq 'top') {
	    $y1 = $self->{y1} - $self->getHeight();
	} elsif ($self->{labelVAlign} eq 'bottom') {
	    $y1 = $self->{y1} - $fh;
	} 
	else {  # centered
	    $y1 = int($self->{y1} - $self->getHeight()/2.0 - $fh/2.0);
	}
	$image->string($font,$x,$y1,$lbl,$gdCanvas->getDefaultFgColor());

	if ($self->{drawBar}) {
	    my $bx1 = $gdCanvas->getScreenCoords()->{x1} - 10;
	    my $rect = new GD::Polygon;
	    $rect->addPt($bx1, $self->{y1});
	    $rect->addPt($bx1+1, $self->{y1});
	    $rect->addPt($bx1+1, $self->{y1} - $self->getHeight());
	    $rect->addPt($bx1, $self->{y1} - $self->getHeight());
	    $image->polygon($rect, $gdCanvas->getDefaultFgColor());
	}
    }
}
