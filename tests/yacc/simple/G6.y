/* G6.y */
/*
 * In states 1 and 4, have 2*2 = 4 shift-reduce conflicts between
 * T -> . a, {...} and
 * n -> . {a}, n -> . num, {a}.
 */

%token num

%%

E : E '+' T n | T ;
T : 'a' | '(' E n ')' | n 'a' ;
n : /* empty */ | num ;

%%

