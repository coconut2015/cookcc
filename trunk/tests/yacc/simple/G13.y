/* Acta Informatica 9, 31-59(1977). Example 3. */

%token c
%token b

%%

G : E c | c E c;
E : A c;
A : B | B E;
B : b b;

%%

