package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import java.util.*

// *****************
// * Contract Code *
// *****************
class ServiceContract : Contract {
    // This is used to identify our contract when building a transaction
    companion object {
        val ID = "com.template.ServiceContract"
    }
    // A transaction is considered valid if the verify() function of the contract of each of the transaction's input
    // and output states does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // this confirms we only have one output
        val output = tx.outputsOfType<ServiceState>().single()
        val input = tx.outputsOfType<ServiceState>().singleOrNull()
        val command = tx.commands.single()

        when(command.value) {
            is Commands.Request -> {
                requireThat {
                    // Lets allow everything through for this
                }
            }

            is Commands.Service -> {
                requireThat {
                    "description is not empty" using (output.description.isNotEmpty())
                    "both service provider and owner are required signers" using (command.signers.containsAll(listOf(output.owner.owningKey, output.serviceProvider.owningKey)))
                }
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Service : Commands
        class Request : Commands
    }
}

// *********
// * State *
// *********
data class CarState(val owner: Party, val VIN: Int, val registration: String, val manufacturer: String, val colour: String, override val linearId: UniqueIdentifier = UniqueIdentifier(registration)) : LinearState {
    override val participants: List<AbstractParty> = listOf()
}

data class ServiceState(val owner: Party, val serviceProvider: Party, val registration: String, val description: String) : ContractState {
    override val participants: List<AbstractParty> = listOf()
}