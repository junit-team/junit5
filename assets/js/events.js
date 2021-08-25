---
# This file has YAML front matter so {{ site.baseurl }} gets replaced in its body.
---

function fillEventList(eventList, events, speakers) {
  var template = _.template('<dt><%= event.date %></dt><dd><strong><%= event.title %></strong> at <strong><a href="<%= event.link %>"><%= event.event %></a></strong> &mdash; <%= event.location %><br><em><%= speakers %></em></dd>');
  _.chain(events)
    .sortBy('date')
    .forEach(function(event) {
      var speakerText = _.map(event.speakers, function(speakerId) {
        if (speakers[speakerId]) {
          return speakers[speakerId].name;
        }
        return speakerId;
      }).join(", ");
      eventList.append(template({'event': event, 'speakers': speakerText}));
    });
};

$(document).ready(function() {
  var events;
  var speakers = {};
  $.when(
    $.getJSON('{{ site.baseurl }}/data/events.json', function(data) {
        events = data;
    }),
    $.getJSON('{{ site.baseurl }}/data/speakers.json', function(data) {
        speakers = data;
    })
  ).then(function() {
    var yesterdayAtSameTime = moment().subtract(1, 'day');
    events = _.filter(events, function(event) {
      return moment(event.date).isAfter(yesterdayAtSameTime);
    });
    var eventsDiv = $('#events');
    if (_.isEmpty(events)) {
      $('#events').append('<p>There are no upcoming events at the moment.</p>');
    }
    else {
      $('#events').append('<dl></dl>')
      var eventList = $('#events dl');
      fillEventList(eventList, events, speakers);
    }
  });
});
