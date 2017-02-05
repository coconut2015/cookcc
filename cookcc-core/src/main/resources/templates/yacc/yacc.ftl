<#if code?has_content && code.fileheader?has_content>
%{
${code.fileheader}
%}
</#if>

<#if tokens?has_content>
<#list tokens as i><#if i.tokens?has_content>
<#if i.type?has_content>%${i.type}<#else>%nonassoc</#if><#list i.tokens as j> ${j}</#list>
</#if></#list>
</#if>
<#if parser.start?has_content>
%start ${parser.start}
</#if>
%%
<#list parser.grammars as grammar>
${grammar.rule}
<#list grammar.rhs as rhs>
	<#if rhs_index == 0>:<#else>|</#if>	${rhs.terms}<#if rhs.precedence?has_content> %prec ${rhs.precedence}</#if>
<#if rhs.action?has_content>
		{${rhs.action}}
</#if>
</#list>
	;

</#list>
%%
<#if code?has_content && code.default?has_content>
${code.default}
</#if>