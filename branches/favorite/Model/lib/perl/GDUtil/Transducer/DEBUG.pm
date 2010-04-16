#!/usr/bin/perl

#------------------------------------------------------------------------
# DEBUG.pm
#
# Used for debugging and profiling; generates files in /tmp 
# (and does not clean them up automatically.)
#
# Y. Thomas Gan 
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::DEBUG;

sub logTS {
    my ($msg, $lastcall, $spacer, $file) = @_;
    $spacer = "" unless $spacer;
    $file = "/tmp/GUSGenomicSeqTransducer.TimeSeries.$$" unless $file;
    open(OUT, ">>$file");
    select(OUT);
    $| = 1;
    select(STDOUT);
    my $time = time;
    my $dt = `date '+%D %T'`;
    chomp $dt;
    my $elapse = "--";
    $elapse = $time - $lastcall if $lastcall >= 0;
    print OUT "[$dt] $spacer ($elapse) $msg\n";
    close OUT;
    return $time;
}

1;
