<b>${editionName} calendar</b>

<#list events as event>
<i>${event.formattedEventDate}</i> - ${event.longEventName}
</#list>
