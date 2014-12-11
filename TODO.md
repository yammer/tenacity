* Ability to request a circuit breaker to close. Then is useful when a node is receiving a small amount of traffic and is an open state.
* When returning a 429, we can use the circuit breaker sleep window to give a reasonable estimate at a Retry-After header hint.
