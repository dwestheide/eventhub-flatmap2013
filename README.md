#About

This is a demo application showcasing the use of the
[Eventsourced library](https://github.com/eligosource/eventsourced) for implementing the Event Sourcing (and
Command Sourcing), CQRS, and Memory Image patterns with Akka actors.

It makes use of the MongoDB journal based on ReactiveMongo for its event store, so you need to run a local MongoDB:

`mongod`

To run the application: 

`sbt run`

#Overview

The core domain of this example application is that of organizing tech events like user group meetings or, possibly, conferences. The application consists of two [bounded contexts](http://dddcommunity.org/uncategorized/bounded-context/) (for more about bounded contexts, see the "Domain-driven Design" book by Eric Evans):

- the _Identity and Access Management (IAM)_ context
- the _Event Planning_ context

Using these two bounded contexts for the example is inspired a little bit by the excellent book "Implementing Domain-driven Design" by Vaugn Vernon.

These two bounded contexts would likely run on separate servers in a real application, the idea being that they are as autonomous as possible. In this example application, they are in the same JVM, to keep the setup simple, but our code doesn't make any assumptions about this.

## IAM

The IAM context is about user registration, authentication and possibly authorization as well as managing user profile information. It is only sketched out in this application with a very simplified implementation of user registration. 

Registered users are held in a `UserRepository` using an STM Ref. The command-sourced `UserProcessor` adds newly registered users to that repository and publishes `UserRegistered` events. These events can be consumed from other bounded contexts, in this case the Event Planning context. 

Event publication takes place by means of an Eventsourced channel, using the functionality of the `Emitter` trait provided by the Eventsourced library for convenience.

## Event Planning

In the event planning context, we are only interested in the domain of planning events/meetings. In this context, we do not want to cope with users and authentication or registration. Instead, we are dealing with _attendees_ who can attend _meetings_. Hence, we have two aggregate types, _Meeting_  and _Attendee_.

###Attendees

Attendees will only be eventually consistent with the actual users in the IAM context: The event-sourced _AttendeeProcessor_ consumes events from the channel that the `UserProcessor` is writing its events to and updates the `AttendeeRepository` accordingly. Again, this repository uses an STM Ref containing a map of all attendees by their identity.

###Meetings
The core functionality is implemented in the _Meeting_ aggregate (in the immutable domain model) and the corresponding application layer service. The aggregate implements all business logic for adding talks, changing venues and declaring or cancelling one's attendance, making sure its business invariants are not violated by any such operation. Hence, all its operations return either an error or a modified copy of themselves.

In the application layer, the `MeetingProcessor`, a command-sourced actor like the `UserProcessor`, processes all of the commands that are possible for a `Meeting` aggregate instance. The pattern is always the same: 

1. Get the aggregate for the id referenced in the command
2. call the appropriate business operation on that aggregate
3. if the operation was allowed
	- save the updated copy of the aggregate back to the repository 
	- emit an appropriate event to a channel
4. send the result (either an error or the updated copy of the aggregate) back to the sender

### Query model projections

The `MeetingProcessor` processes commands and stores application state in an STM-based repository, implementing the Memory Image pattern. However, it also emits events. Hence, it is basically a command handler in the CQRS sense. 

And while it is perfectly possible to send the in-memory Meeting aggregates to clients, we can also use the emitted events in order to build up dedicated query models.

An example of for how this can be achieved is in the `MeetingStatsEventHandler`: Every time it receives an event, it updates the meeting stats query model, which provides a view on how many people declared or cancelled their attendance by day. In CQRS, this is usually stored in some database, but to keep the application simple, we are using an STM Ref again.










