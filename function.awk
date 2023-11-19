
BEGIN{
    FS = ", ";
}

function func(a, b){
    charA = substr(a, 1, 0);
    charB = substr(b, 1, 0);

    printf "%c occurs in %s at index %d\n", charA, b, index(b, charA);
    printf "%c occurs in %s at index %u\n", charB, a, index(a, charB);
}

{
    func($1, $2);
}