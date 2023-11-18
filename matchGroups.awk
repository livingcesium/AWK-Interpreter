{
    if (match(tolower($0), `(te)(st)`, groups)) {
        print "Line:", NR, "- Match found\n";
        print "  Full match:", groups[1];
        print "  First group (te):", groups[2];
        print "  Second group (st):", groups[3];
        print "\n";
    } else {
        print "Line:", NR, "- No match found\n";
    }
}