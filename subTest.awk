{
    # Print the current line
    print $0 "\n";

    while (getline(line) > 0) {
        # Use gsub to switch A's and B's in the next line(s). Depends on whether $0 has As or Bs in it, with As taking priority.
        if(match $0, `A+`)
            line = gsub(`B`, "A", line)
        else if (match $0, `B+`)
            line = gsub(`A`, "B", line)

        # Print the modified line
        print line "\n";
    }
}