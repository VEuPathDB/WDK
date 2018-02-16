#!/usr/bin/perl
 
use strict;
 
#------------------------- genRemakeSeqsCmd.pl ---------------------
 
# Check Command Line Arguments
 
if ($#ARGV != 0) {
       die "Invalid arguments!\n";
}
 
my $tnsName = $ARGV[0];
 
#--------------------- Initialize sequence info ---------------------
 
my $seqInfo = ();
 
$seqInfo ->{1} [0] = 'announce.messages_id_pkseq';
$seqInfo ->{1} [1] = 'MESSAGE_ID';
$seqInfo ->{1} [2] = 'announce.MESSAGES';
 
$seqInfo ->{2} [0] = 'announce.projects_id_pkseq';
$seqInfo ->{2} [1] = 'PROJECT_ID';
$seqInfo ->{2} [2] = 'announce.PROJECTS';
 
$seqInfo ->{3} [0] = 'announce.category_id_pkseq';
$seqInfo ->{3} [1] = 'CATEGORY_ID';
$seqInfo ->{3} [2] = 'announce.CATEGORY';
 
 
$seqInfo ->{6} [0] = 'userlogins5.users_pkseq';
$seqInfo ->{6} [1] = 'USER_ID';
$seqInfo ->{6} [2] = 'userlogins5.users';
 
$seqInfo ->{7} [0] = 'userlogins5.strategies_pkseq';
$seqInfo ->{7} [1] = 'strategy_id';
$seqInfo ->{7} [2] = 'userlogins5.strategies';
 
$seqInfo ->{8} [0] = 'userlogins5.steps_pkseq';
$seqInfo ->{8} [1] = 'step_id';
$seqInfo ->{8} [2] = 'userlogins5.steps';
 
$seqInfo ->{9} [0] = 'userlogins5.datasets_pkseq';
$seqInfo ->{9} [1] = 'dataset_id';
$seqInfo ->{9} [2] = 'userlogins5.datasets';
 
$seqInfo ->{10} [0] = 'userlogins5.dataset_values_pkseq';
$seqInfo ->{10} [1] = 'dataset_value_id';
$seqInfo ->{10} [2] = 'userlogins5.dataset_values';
 
$seqInfo ->{11} [0] = 'userlogins5.user_baskets_pkseq';
$seqInfo ->{11} [1] = 'basket_id';
$seqInfo ->{11} [2] = 'userlogins5.user_baskets';
 
$seqInfo ->{12} [0] = 'userlogins5.favorites_pkseq';
$seqInfo ->{12} [1] = 'favorite_id';
$seqInfo ->{12} [2] = 'userlogins5.favorites';
 
$seqInfo ->{13} [0] = 'userlogins5.categories_pkseq';
$seqInfo ->{13} [1] = 'category_id';
$seqInfo ->{13} [2] = 'userlogins5.categories';
 
$seqInfo ->{14} [0] = 'userlogins5.step_analysis_pkseq';
$seqInfo ->{14} [1] = 'ANALYSIS_ID';
$seqInfo ->{14} [2] = 'userlogins5.step_analysis';
 
$seqInfo ->{15} [0] = 'userlogins5.commentStableId_pkseq';
$seqInfo ->{15} [1] = 'COMMENT_STABLE_ID';
$seqInfo ->{15} [2] = 'userlogins5.commentStableId';
 
$seqInfo ->{16} [0] = 'userlogins5.commentTargetCategory_pkseq';
$seqInfo ->{16} [1] = 'COMMENT_TARGET_CATEGORY_ID';
$seqInfo ->{16} [2] = 'userlogins5.commentTargetCategory';
 
$seqInfo ->{17} [0] = 'userlogins5.commentReference_pkseq';
$seqInfo ->{17} [1] = 'COMMENT_REFERENCE_ID';
$seqInfo ->{17} [2] = 'userlogins5.commentReference';
 
$seqInfo ->{18} [0] = 'userlogins5.commentFile_pkseq';
$seqInfo ->{18} [1] = 'FILE_ID';
$seqInfo ->{18} [2] = 'userlogins5.commentFile';
 
$seqInfo ->{19} [0] = 'userlogins5.commentSequence_pkseq';
$seqInfo ->{19} [1] = 'COMMENT_SEQUENCE_ID';
$seqInfo ->{19} [2] = 'userlogins5.commentSequence';
 
$seqInfo ->{20} [0] = 'userlogins5.comments_pkseq';
$seqInfo ->{20} [1] = 'COMMENT_ID';
$seqInfo ->{20} [2] = 'userlogins5.comments';
 
$seqInfo ->{21} [0] = 'userlogins5.locations_pkseq';
$seqInfo ->{21} [1] = 'LOCATION_ID';
$seqInfo ->{21} [2] = 'userlogins5.locations';
 
$seqInfo ->{22} [0] = 'userlogins5.external_databases_pkseq';
$seqInfo ->{22} [1] = 'EXTERNAL_DATABASE_ID';
$seqInfo ->{22} [2] = 'userlogins5.external_databases';
 
#--------------------------------------------------------------------
 
my $systemPw = "eto5A91L";
my ($cmdFile,$nextVal,$seqName);
my $cmdFileTxt = "";
my $tnsNameUc = uc($tnsName);
 
if ($tnsNameUc eq 'CECOMMS') { $cmdFile = "remake_cecommnSequences.sql"; }
elsif ($tnsNameUc eq 'CECOMMN') { $cmdFile = "remake_cecommsSequences.sql"; }
 
my %nextSeqVals = getSeqVals($tnsName,$seqInfo);
 
foreach $seqName (keys %nextSeqVals) {
       $nextVal = $nextSeqVals{$seqName};
 
       $cmdFileTxt .= "drop sequence $seqName;\n"
                  . "create sequence $seqName start with $nextVal increment by 10 nomaxvalue;\n"
                  . "grant select on $seqName to COMM_WDK_W;\n";
}
 
system("rm $cmdFile");
 
makeTmpFile($cmdFile,$cmdFileTxt);
 
#-------------------------------- SUBS ------------------------------
 
sub getSeqVals {
       my ($tnsName,$seqInfo) = @_;
       my %nextSeqVals = ();
       my ($nvl,$offset);
       my ($pkCol,$seqName,$seqVal,$tblName);
       my $cmdFile = "getSeqValsTmp.sql";
       my $cmdFileTxt = '';
       my $cmdOutLn;
       my $tnsNameUc = uc($tnsName);
      
       if ($tnsNameUc eq "CECOMMN") { $nvl = 3; $offset = 3; }
       elsif ($tnsNameUc eq "CECOMMS") { $nvl = 10; $offset = 0; }
 
       for (my $i=0;$i<=23;$i++) {
              $seqName = $seqInfo ->{$i} [0];
              $pkCol = $seqInfo ->{$i} [1];
              $tblName = $seqInfo ->{$i} [2];
 
              $cmdFileTxt .= "select '<' || 'row' || '>' || '$seqName<field>' || to_char(nvl(max($pkCol) + 10,$nvl)) "
                         . "from $tblName where mod($pkCol,10) = $offset;\n\n";
       }
 
       system("rm $cmdFile");
       makeTmpFile($cmdFile,$cmdFileTxt);
 
       my $cmd = "sqlplus system/$systemPw\@" . "$tnsName < $cmdFile";
 
       my $cmdOut = `$cmd`;
 
       # print "$cmdOut\n"; # TEMPORARY
 
       my @cmdOutAry1 = split("\n",$cmdOut);
 
       my @cmdOutAry2 = ();
 
       for (my $j=0;$j<=$#cmdOutAry1;$j++) {
              $cmdOutLn = $cmdOutAry1[$j];
 
              if ($cmdOutLn =~ /<row>/) {
                     $cmdOutLn = substr($cmdOutLn,5);
                     @cmdOutAry2 = split(/<field>/,$cmdOutLn);
                     $seqName = $cmdOutAry2[0];
                     $seqVal = $cmdOutAry2[1];
                     $nextSeqVals{$seqName} = $seqVal;
 
                     # print "$seqName, $seqVal\n";     # TEMPORARY
              }
       }
 
 
       return %nextSeqVals;
}
 
#--------------------------------------------------------------------
 
sub makeTmpFile {
       my ($fileName,$fileTxt) = @_;
      
       open (TMP, "> $fileName")
              or die "Couldn't open $fileName for writing: $!\n";    
       print  TMP "$fileTxt";
       close(TMP);
}
 
#----------------------------------------------------------
 