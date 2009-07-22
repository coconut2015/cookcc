/* G7 */

%%

S : 'd' 'i' A ;
A : A T | /* empty */ ;
T : M | Y | P | B ;
M : 'r' | 'c' ;
Y : 'x' | 'f' ;
P : 'n' | 'o' ;
B : 'a' | 'e' ;

%%

