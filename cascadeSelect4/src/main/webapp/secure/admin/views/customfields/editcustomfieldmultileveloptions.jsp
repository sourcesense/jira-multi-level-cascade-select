<%@ taglib uri="webwork" prefix="webwork" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><webwork:text name="'admin.issuefields.customfields.edit.options'"/></title>
</head>
<script language="JavaScript">
<!--
function loadUri(optionId)
{
    window.location = '<webwork:property value="./selectedParentOptionUrlPreifx" escape="false" />' + optionId;
    return true;
}
//-->
</script>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><webwork:text name="'admin.issuefields.customfields.edit.options'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="instructions">
    <p>
        <webwork:if test="/selectedParentOption">
            <webwork:text name="'admin.issuefields.customfields.reorder.parent'">
                <webwork:param name="'value0'"><strong><webwork:property value="/fieldConfig/name" /></strong></webwork:param>
                <webwork:param name="'value1'"><strong><webwork:property value="/customField/name" /></strong></webwork:param>
                <webwork:param name="'value2'"><strong><webwork:property value="/selectedParentOption/value" /></strong></webwork:param>
            </webwork:text>
        </webwork:if>
        <webwork:else>
            <webwork:text name="'admin.issuefields.customfields.reorder'">
                <webwork:param name="'value0'"><strong><webwork:property value="/fieldConfig/name" /></strong></webwork:param>
                <webwork:param name="'value1'"><strong><webwork:property value="/customField/name" /></strong></webwork:param>
            </webwork:text>
        </webwork:else>
    </p>
    <p><webwork:text name="'admin.issuefields.customfields.html.usage'"/></p>
    <ul class="square">
        <li><a title="<webwork:text name="'admin.issuefields.customfields.sort.alphabetically'"/>" href="<webwork:property value="/selectedParentOptionUrlPrefix('sort')" /><webwork:property value="/selectedParentOptionId" />"><webwork:text name="'admin.issuefields.customfields.sort.alphabetically'"/></a></li>
        <li><a title="<webwork:text name="'admin.issuefields.customfields.view.custom.field.configuration'"/>" href="ConfigureCustomField!default.jspa?customFieldId=<webwork:property value="/customField/idAsLong"/>"><webwork:text name="'admin.issuefields.customfields.view.custom.field.configuration'"/></a></li>
    </ul>
<webwork:if test="/cascadingSelect == true">
<p>
    <webwork:text name="'admin.issuefields.customfields.choose.parent'"/>:
    <select name="<webwork:property value="./customFieldHelper/id" />" onchange="return loadUri(this.value);">
        <option value=""><webwork:text name="'admin.issuefields.customfields.edit.parent.list'"/></option>


        <webwork:if test="/selectedParentOptionId">
            <webwork:if test="/selectedParentOption/parentOption">
                <webwork:iterator value="/selectedParentOption/parentOption/childOptions" status="'rowStatus'">
                    <option value="<webwork:property value="./optionId" />" <webwork:if test="./optionId == /selectedParentOptionId">selected</webwork:if>>
                        <webwork:property value="./value" />
                    </option>
                </webwork:iterator>
            </webwork:if>
            <webwork:else>
                <webwork:iterator value="/options" status="'rowStatus'">
                    <option value="<webwork:property value="./optionId" />" <webwork:if test="./optionId == /selectedParentOptionId">selected</webwork:if>>
                        <webwork:property value="./value" />
                    </option>
                </webwork:iterator>
            </webwork:else>
        </webwork:if>
        <webwork:else>
            <webwork:iterator value="/options" status="'rowStatus'">
                <option value="<webwork:property value="./optionId" />" <webwork:if test="./optionId == /selectedParentOptionId">selected</webwork:if>>
                    <webwork:property value="./value" />
                </option>
            </webwork:iterator>
        </webwork:else>

    </select>
</p>
</webwork:if>


</page:param>


<webwork:if test="/displayOptions && /displayOptions/empty == false">

    <form name="configureOption" action="ConfigureCustomFieldOptions.jspa" method="post">
    <table class="grid maxWidth minColumns">
    <tr>
            <th>
                <webwork:text name="'admin.issuefields.customfields.position'"/>
            </th>
            <th class="normal">
                <webwork:text name="'admin.issuefields.customfields.option'"/>
            </th>
            <webwork:if test="/displayOptions/size > 1">
                <th class="fullyCentered">
                    <webwork:text name="'admin.issuefields.customfields.order'"/>
                </th>
                <th nowrap>
                    <webwork:text name="'admin.issuefields.customfields.move.to.position'"/>
                </th>
            </webwork:if>
            <th>
                <webwork:text name="'common.words.operations'"/>
            </th>
        </tr>

        <webwork:iterator value="/displayOptions" status="'status'">
        <tr class="<webwork:if test="/hlOptions/contains(./value) == true">rowHighlighted</webwork:if><webwork:elseIf test="@status/odd == true">rowNormal</webwork:elseIf><webwork:else>rowAlternate</webwork:else>">
            <td>
                <webwork:property value="@status/count" />.
            </td>
            <ui:textfield name="/newLabelTextBoxName(./optionId)" label="Update label" theme="'single'" value="./value" size="'30'"/>
<%--
            <td class="normal">
            <webwork:if test="/cascadingSelect == true">
                <a title="Edit children options for <webwork:property value="./value" />" href="<webwork:property value="/selectedParentOptionUrlPreifx" escape="false" /><webwork:property value="./optionId" />">
            </webwork:if>
                <b><webwork:property value="./value" /></b>
                <span class="smallgrey"><webwork:if test="/defaultValue(./optionId/toString()) == true">(<webwork:text name="'admin.common.words.default'"/>)</webwork:if></span>
            <webwork:if test="/cascadingSelect == true && !/selectedParentOptionId"></a></webwork:if>
            </td>
--%>
            <webwork:if test="/displayOptions/size > 1">
                <td valign=top align=center nowrap>
                    <webwork:if test="@status/first != true">
                    <a id="moveToFirst_<webwork:property value="./optionId" />" href="<webwork:property value="/doActionUrl(.,'moveToFirst')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_first.gif" border=0 width=16 height=16 title="<webwork:text name="'admin.issuefields.customfields.move.to.first.position'"/>"></a>
                    <a id="moveUp_<webwork:property value="./optionId" />" href="<webwork:property value="/doActionUrl(.,'moveUp')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_up_blue.gif" border=0 width=16 height=16 title="<webwork:text name="'admin.issuefields.customfields.move.this.option.up'"/>"></a></webwork:if>
                    <webwork:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=13 height=14><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=20 height=16></webwork:else>
                    <webwork:if test="@status/last != true">
                    <a id="moveDown_<webwork:property value="./optionId" />" href="<webwork:property value="/doActionUrl(.,'moveDown')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_down_blue.gif" border=0 width=16 height=16 title="<webwork:text name="'admin.issuefields.customfields.move.this.option.down'"/>"></a>
                    <a id="moveToLast_<webwork:property value="./optionId" />" href="<webwork:property value="/doActionUrl(.,'moveToLast')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_last.gif" border=0 width=16 height=16 title="<webwork:text name="'admin.issuefields.customfields.move.this.option.to.last'"/>"></a></webwork:if>
                    <webwork:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=13 height=14><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=20 height=16></webwork:else>
                </td>

                <ui:textfield name="/newPositionTextBoxName(./optionId)" label="text('admin.issuefields.customfields.new.option.position')" theme="'single'" value="/newPositionValue(./optionId)" size="'2'">
                    <ui:param name="'class'">fullyCentered</ui:param>
               </ui:textfield>
            </webwork:if>
            <td valign=top nowrap>
<%--
            <webwork:if test="/cascadingSelect == true && !/selectedParentOptionId">
--%>
            <webwork:if test="/cascadingSelect == true">
                <a title="<webwork:text name="'admin.issuefields.customfields.edit.children.options'">
                    <webwork:param name="'value0'"><webwork:property value="./value" /></webwork:param>
                </webwork:text>" href="<webwork:property value="/selectedParentOptionUrlPreifx" escape="false" /><webwork:property value="./optionId" />"><webwork:text name="'common.words.edit'"/></a>&nbsp;|
            </webwork:if>
            <webwork:if test="/defaultValue(./optionId/toString()) != true">
                <a id="del_<webwork:property value="./value"/>" href="<webwork:property value="/doActionUrl(.,'remove')" escape="false" />"><webwork:text name="'common.words.delete'"/></a>
            </webwork:if>
            <webwork:else>&nbsp;</webwork:else>
            </td>
        </tr>
        </webwork:iterator>
            <tr class="rowHeader" align="center">
                <td>&nbsp;
                    <input type="hidden" name="id" value="<webwork:property value="/id" />">
                    <input type="hidden" name="fieldConfigId" value="<webwork:property value="/fieldConfigId" />">
                    <input type="hidden" name="selectedParentOptionId" value="<webwork:property value="/selectedParentOptionId" />">
                </td>
                <td>
                    <input type="submit" name="saveLabel" value="<webwork:text name="'common.words.update'"/>">
                </td>
                <td>&nbsp;</td>
                <webwork:if test="./displayOptions/size > 1">
                    <td>
                        <input type="submit" name="moveOptionsToPosition" value="<webwork:text name="'common.forms.move'"/>">
                    </td>
                </webwork:if>
                <td></td>
            </tr>
    </table>
    </form>

</webwork:if>
<webwork:else>
    <p style="padding: 10px 0;"><webwork:text name="'admin.issuefields.customfields.currently.no.options'"/></p>
</webwork:else>


    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditCustomFieldMultiLevelOptions!add.jspa</page:param>
        <page:param name="submitName"><webwork:text name="'common.forms.add'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><webwork:text name="'admin.issuefields.customfields.add.new.option'"/></page:param>
	    <page:param name="buttons">&nbsp;<input type="button" value="Done" onclick="location.href='ConfigureCustomField!default.jspa?customFieldId=<webwork:property value="/customField/idAsLong" />'">&nbsp;</page:param>

        <ui:textfield label="text('admin.issuefields.customfields.add.value')" name="'addValue'" />
        <ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'selectedParentOptionId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'addSelectValue'" value="true" template="hidden.jsp" theme="'single'"  />
    </page:applyDecorator>
    </p>
</page:applyDecorator>

</body>
</html>