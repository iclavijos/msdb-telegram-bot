You are currently subscribed, and receive notifications 15 minutes, 1 hours and/or 3 hours before each session, depending on your settings, of:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Races
<#if subscription.notifyQualifying = true>- Qualifying sessions</#if>
<#if subscription.notifyPractice = true>- Practice sessions</#if>
</#list>
