<#setting number_format=0>
<#macro array a>
<#list a as i><#if i_index &gt; 0>,</#if>${i}</#list>
</#macro>
<#macro intarray a>
<#list a as i><#if i_index &gt; 0>,</#if>${i}</#list>
</#macro>
<#macro intarray3d b>
<#list b as a>
	<@intarray a/>
</#list>
</#macro>
<#if lexer?has_content>
# lexer
# properties
: unicode = ${unicode?string}
: bol = ${lexer.bol?string}
: backup = ${lexer.backup?string}
: cases = ${lexer.caseCount}
: table = ${lexer.table}
<#if lexer.table == "ecs" || lexer.table == "compressed">
: ecs = ${lexer.ecsGroupCount}
</#if>
: states = ${lexer.dfa.size}
# memory usage
: full table = ${((lexer.eof + 1) * lexer.dfa.size)}
<#if lexer.table == "ecs" || lexer.table == "compressed">
: ecs table = ${(lexer.eof + 1 + lexer.ecsGroupCount * lexer.dfa.size)}
</#if>
<#if lexer.table == "compressed">
: next = ${lexer.dfa.next?size}
: check = ${lexer.dfa.check?size}
<#if !lexer.dfa.default?has_content>
: compressed table = ${(lexer.eof + 1 + lexer.dfa.next?size + lexer.dfa.next?size)}
<#else>
: default = ${lexer.dfa.default?size}
<#if !lexer.dfa.meta?has_content>
: compressed table = ${(lexer.eof + 1 + lexer.dfa.next?size + lexer.dfa.next?size + lexer.dfa.default?size)}
<#else>
: meta = ${lexer.dfa.meta?size}
: compressed table = ${lexer.eof + 1 + lexer.dfa.next?size + lexer.dfa.next?size + lexer.dfa.default?size + lexer.dfa.meta?size}
</#if>
</#if>
</#if>
<#if lexer.table == "ecs" || lexer.table == "compressed">
# ecs
<@intarray lexer.dfa.ecs/>
</#if>
# dfa
<#if lexer.table == "ecs" || lexer.table == "full">
<@intarray3d lexer.dfa.table/>
</#if>
<#if lexer.table == "compressed">
# compressed correctly = ${lexer.dfa.correct?string}
# base
<@intarray lexer.dfa.base/>
# next
<@intarray lexer.dfa.next/>
# check
<@intarray lexer.dfa.check/>
<#if lexer.dfa.default?has_content>
# default
<@intarray lexer.dfa.default/>
</#if>
<#if lexer.dfa.error>
: error = ${lexer.dfa.error?string}
</#if>
<#if lexer.dfa.meta?has_content>
# meta
<@intarray lexer.dfa.meta/>
</#if>
</#if>
# states
<@array lexer.states/>
# begins
<@intarray lexer.begins/>
# accepts
<@intarray lexer.accept/>
# cases
<#list lexer.cases as i>
<#list i.patterns as p>
# case ${p.caseValue}
{${i.action}}
</#list>
</#list>
# end
</#if>
<#if parser?has_content>
# parser
# properties
: symbols = ${parser.symbols?size}
: max terminal = ${parser.maxTerminal}
: used terminals = ${parser.usedTerminalCount}
: non-terminals = ${parser.nonTerminalCount}
: rules = ${parser.rules?size - 1}
: shift/reduce conflicts = ${parser.shiftConflict}
: reduct/reduce conflicts = ${parser.reduceConflict}
# memory usage
: ecs table = ${(parser.ecs?size + (parser.usedTerminalCount + parser.nonTerminalCount) * parser.dfa.size)}
<#if parser.table == "compressed">
: compressed table = ${parser.ecs?size + parser.dfa.totalSize}
</#if>
# ecs
<@intarray parser.ecs/>
<#if parser.table == "ecs">
# table
<@intarray3d parser.dfa.table/>
<#else>
# compressed correctly = ${parser.dfa.correct?string}
# base add
${parser.dfa.baseAdd}
# base
<@intarray parser.dfa.base/>
# next
<@intarray parser.dfa.next/>
# check
<@intarray parser.dfa.check/>
<#if parser.dfa.default?has_content>
# default
<@intarray parser.dfa.default/>
</#if>
<#if parser.dfa.error>
: error = ${parser.dfa.error?string}
</#if>
<#if parser.dfa.meta?has_content>
# meta
<@intarray parser.dfa.meta/>
</#if>
<#if parser.dfa.gotoDefault?has_content>
# goto default
<@intarray parser.dfa.gotoDefault/>
</#if>
</#if>
# rules
<@intarray parser.rules/>
# cases
<#list parser.cases as i>
<#list i.rhs as p>
# case ${p.caseValue}
{${p.action}}
</#list>
</#list>
</#if>
