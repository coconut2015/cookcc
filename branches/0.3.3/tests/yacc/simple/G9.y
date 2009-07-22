/* G9 */

%token num

%%

E : E '+' T | T ;
T : 'a' | num | '(' E ')' ;

%%

