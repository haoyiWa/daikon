#!/usr/bin/env perl

# Adds nonces to a dtrace file.  Reads trace file from STDIN and
# writes new trace file to STDOUT.

use English;
use strict;
$WARNING = 1;

my $count = 0;
# contains elements of the form "$nonce|$function"
my @stack = ();

while (<>) {
  # print the original line
  print;

  # print the nonce
  if (/^(.*):::(ENTER|EXIT)/) {
    if ($2 eq "ENTER") {
      print "this_invocation_nonce\n";
      print "$count\n";
      push @stack, "$count|$1";
      $count++;
    } elsif ($2 eq "EXIT") {
      my $nonce = find_nonce($1);
      print "this_invocation_nonce\n";
      print "$nonce\n";      
    } else {
      die "Error: Invalid ppt type: $2\n";
    }
  }
}


# finds the nonce for a function, popping the stack until the function
# is found
sub find_nonce {
  my ($target_function) = @_;
  my $function = "";
  my $nonce = "";
  while ($function ne $target_function) {
    if (@stack == 0) {
      die "ERROR: Stack is empty\n";
    }
    my $element = pop @stack;
    ($nonce, $function) = split /\|/, $element;
  }
#  print "find_nonce returning: $nonce\n";
  return $nonce;
}


sub print_stack {
  foreach my $elem (@stack) {
    print "$elem\n";
  }
  print "\n";
}
