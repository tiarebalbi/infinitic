![scarf pixel](https://static.scarf.sh/a.png?x-pxid=b7a9c0b3-ae8b-4e19-838b-36a40ee1cf96)

# Infinitic

[Infinitic](https://infinitic.io), while already used in production for years, is still in active development.
Subscribe [here](https://infinitic.substack.com) to stay up-to-date.

## What is it?

Infinitic accelerates the implementation of event-driven architectures, reducing development time from months to
weeks. It is ideal for backend teams developing applications that are time-dependant, dynamic or including human
interactions, such as:

* E-commerce platforms with dynamic inventory management and complex delivery and invoicing processes
* Financial systems handling multi-step transactions with built-in error recovery, or even manual approval for large
  transfers
* Supply chain management systems coordinating time-critical logistics across multiple parties
* Customer support platforms with escalation processes that evolve based on issue complexity and involve human agents
  for complex cases
* Subscription-based services managing recurring billing cycles with retry mechanisms and manual review for payment
  disputes
* Travel booking systems orchestrating flight, hotel, and car reservations with fallback options
* Content management systems with multi-stage approval workflows, scheduled publishing, and editorial review processes

Building with Infinitic requires only two key components:

- A **database** for storing current workflow states using a simple key-value table (currently supporting Redis,
  Postgres, and MySQL, with easy expansion to other databases).
- An **event streaming platform** to securely store and distribute messages generated by Infinitic to workers (currently
  supporting [Apache Pulsar](https://pulsar.apache.org/)).

Don't worry about mastering these external dependencies - Infinitic handles everything for you. If you're new to these
technologies, [managed instances](https://docs.infinitic.io/docs/references/pulsar#using-infinitic-with-third-party-providers)
are an excellent solution.

Yourself only need to focus on writing your application-specific code:

- **Workflow Workers** orchestrate the execution of your Services. You simply code their description using Service
  interfaces.
- **Service Workers** execute your Services. You implement the actual Service logic or API calls, while Infinitic
  handles all Pulsar-related operations.

Those workers built with the Infinitic SDK - consume messages, process them, and produce responses. The Infinitic SDK
provides extensive functionalities that you do not have to code and maintain:

- Implementation of events consumers and producers
- Management of topics: topology, schemas, and creation
- Error handling in case of failures
- Data serialization
- API for building workflows

At last, if you already have an existing infrastructure, Infinitic doesn't require you to replace it. Instead, it can
seamlessly integrate with your current setup, allowing you to develop new features and enhance your system's
capabilities without disrupting your existing architecture.

Key benefits of using Infinitic:

* **Flexibility**: Harness the full power of programming languages with loops, conditions, and data manipulation to
  define durable processes.
* **Easy Maintenance**: Workflows are easy to understand, consolidated in a single class, and version-controlled like
  standard code.
* **Enhanced Observability**: Comprehensive monitoring with detailed insights available on dashboards.
* **Unmatched Reliability**: Workflows remain resilient to service or worker failures, ensuring consistent operation.
* **High Scalability**: Infinitic's core event-driven architecture guarantees exceptional scalability.

## Getting Started

See the [documentation](https://docs.infinitic.io).
