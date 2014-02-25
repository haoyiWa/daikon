#!/usr/bin/env perl

my $machine_type;

chomp($machine_type = `uname -m`);

while (<>) {
    s/: +[0-9]+ Aborted +/: Aborted: /;
    s/\(core dumped\) *//;
    s/==\d+==/==PID==/;

    if ($machine_type eq "x86_64") {
        # Valgrind AMD64/Linux locations
        s/0xff([0-9a-f]{7,10})/<STACK_ADDR>/ig;  # stack
        s/0x8[01]([0-9a-f]{7})/<STATIC_ADDR>/ig; # r/w data
        s/0x4[cd]([0-9a-f]{5})/<HEAP_ADDR>/ig;   # heap  ArrayTest because so much allocated?
        s/0x5[1a]([0-9a-f]{5})/<HEAP_ADDR>/ig;   # heap
        s/0x4[01]([0-9a-f]{4})/<STATIC_ADDR>/ig; # r/o data
        s/0x6[01]([0-9a-f]{4})/<STATIC_ADDR>/ig; # r/w data
    } else {
        # we just assume ...
        # Valgrind X86/Linux locations
        s/0xb[ef]([0-9a-f]{6})/<STACK_ADDR>/ig;  # stack
        s/0x6[123]([0-9a-f]{6})/<STATIC_ADDR>/ig;# r/w data
        s/0x4[0-8]([0-9a-f]{5})/<HEAP_ADDR>/ig;  # heap
        s/0x8[01]([0-9a-f]{5})/<STATIC_ADDR>/ig; # r/o and r/w data
    }    

# old/unused address substitutions
#        s/0xf[ef]([0-9a-f]{6})/<STACK_ADDR>/ig;  # Valgrind 32-on-64/Linux location
#        s/0x6[0-3]([0-9a-f]{5})/<HEAP_ADDR>/ig;  # Valgrind 32-on-64/Linux location
#        s/0x5[01]([0-9a-f]{4})/<STATIC_ADDR>/ig; # Valgrind AMD64/Linux data


    s/[0-9]:[0-9]{2}:[0-9]{2}/<TIME>/g;      
    s/(.+)Time:(.*)seconds(.*)/<TIME>/g;  
    s/(.+)Rendering line(.*)/<TIME>/g;        # Time (povray prints time taken to execute)

    s/Invalid read of.*/<MEMCHECK ERROR>/g;                # povray has multiple memcheck errors
    s/Address .* bytes after a block .*/<MEMCHECK_ERROR>/g; # replace the information with generic
                                                           # sizes and addresses. Don't remove completely though
                                                           # as we'd still like them to be around to catch errors
                                                           # in other tests

    s/Total Alloc.*/<Allocation statistics>/g;
    s/Peak memory.*/<Allocation statistics>/g;

    s/kvasir-[\d.]+,/kvasir-VERSION/;
    s[Using Valgrind-.* and LibVEX; rerun with \-h for copyright info]
      [Using Valgrind and LibVEX; rerun with \-h for copyright info];
    s/\(vg_replace_malloc.c:(\d+)\)/(vg_replace_malloc.c:XXX)/;
    print;
}
