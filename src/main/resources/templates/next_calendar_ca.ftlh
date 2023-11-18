<b>Calendari de ${editionName}</b>

<#list events as event>
<i>${event.formattedEventDate}</i> - ${event.longEventName}
</#list>
