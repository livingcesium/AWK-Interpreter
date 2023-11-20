
BEGIN{
    FS = ", ";
    i = 0;
}

function func(a, b){
    charA = substr(a, 1, 0);
    charB = substr(b, 1, 0);

    printf "\t%c occurs in %s at index %d\n", charA, b, index(b, charA);
    printf "\t%c occurs in %s at index %u\n", charB, a, index(a, charB);
}

{
    # The array stuff is just nonsense, but it shows multi-dimensional arrays working
    arr[$0][$1][$2] = "test " ++i;
    if(($0,$1,$2) in arr)
        printf "arr[%s][%s][%s] = %s\n", $0, $1, $2, arr[$0][$1][$2];
    func($1, $2);
}