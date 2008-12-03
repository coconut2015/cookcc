/*
 * 611 A6. 
 * prog : prog stmt | stmt
 * stmt : in_stmt | out_stmt
 * in_stmt : Input Id
 * out_stmt : Output expr
 * expr : expr '+' Id | Id | e
 *
 * Let A = prog, B = stmt, C = in_stmt, D = out_stmt,
 * x = Input, y = Id, s = output, E = expr
 */

%token x
%token y
%token s

%%

A : A B | B ;
B : C | D ;
C : x y ;
D : s E ;
E : E '+' y | y | ;

%%

