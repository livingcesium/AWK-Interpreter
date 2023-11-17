BEGIN {
	pos_count = 0;
    neg_count = 0;
}

{
	# Get positive or negative number from every field
    for (i = 1; i <= NF; i++) {
        if ($i > 0) {
            positive[++pos_count] = $i;
        } else if ($i < 0) {
            negative[++neg_count] = $i;
        }
    }
}

END {
    # Find the longer of the two arrays
    max_count = pos_count > neg_count ? pos_count : neg_count;
    
    # Print the two columns
    print "pos\tneg\n";
	for (j = 1; j <= max_count; j++) {
		posStr = j > pos_count ? "" : positive[j];
		negStr = j > neg_count ? "" : negative[j];
		printf "%s\t%s\n", posStr, negStr;
    }
}