/* http://plus.kaist.ac.kr/~shoh/ocaml/ocamllex-ocamlyacc/ocamlyacc-tutorial/sec-mysterious-reduce-reduce-conflicts.html */

%token ID COMMA COLON

%%
def:    param_spec return_spec COMMA
        ;
param_spec:
             type
        |    name_list COLON type
        ;
return_spec:
             type
        |    name COLON type
        ;
type:        ID
        ;
name:        ID
        ;
name_list:
             name
        |    name COMMA name_list
        ;