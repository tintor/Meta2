package tintor.apps.peg;

// program := { class / func / statement }
// class := class_head Separator Begin func* End
// class_head := Class Space Name [ Space Extends Space Name ] [ Space Implements Space Name { Comma Space Name } ]
// func := Def Space Name ( LParen Name { Space Comma Name } RParen ) Separator block
// block := Begin statement* End

// statement := if / while / proc_call / assignment / (Yield / Return) expr?
// proc_call := Name expr_list? Separator
// if := If Space expr Separator block { Else If Space expr Separator block } [ Else Separator block ]
// while := While Space expr Separator block

// assignment := expr [ (Space Equals Space / Equals) expr ] (Separator / Do [ Space Pipe expr_list Pipe ] Separator block)
// expr	:= expr2 { (Space op_add Space / op_add) expr2 }
// expr2 := expr3 { (Space op_mul Space / op_mul) expr3 }
// expr3 := expr4 { Dot Name opt_args }
// expr4 := atom { LBracket expr_list  RBracket }
// atom := call / Integer [ Dot Integer ] / String / Regexp / LParen expr RParen / list / map / range
// call := Name opt_args [ Space LBrace [ Pipe expr_list Pipe ] expr RBrace ]

// op_add := Add / Sub
// op_mul := Mul / Div / Mod

// opt_args := [ LParen expr_list RParen ]
// expr_list := expr { Comma Space expr }

// list := LBracket [ expr { Comma Space expr } ] RBracket
// map := LBrace [ key_value { Comma Space key_value } ] RBrace
// key_value := expr (Space Colon Space / Colon) expr

public class Parser {
	public static void main(final String[] args) {
	}
}