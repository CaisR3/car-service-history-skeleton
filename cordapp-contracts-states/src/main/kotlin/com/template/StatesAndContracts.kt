package com.template

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

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
                    "owner is a required signer" using (command.signers.contains(output.owner.owningKey))
                }
            }

            is Commands.Service -> {
                requireThat {
                    "description is not empty" using (output.servicesProvided != null)
                    "service provider is a required signer" using (command.signers.contains(output.mechanic.owningKey))
                    "input is consumed and output created" using (input != null)
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

data class ServiceState(val owner: Party, val mechanic: Party, val registration: String, val serviced: Boolean = false, val servicesProvided: String? = null) : ContractState {
    override val participants = listOf(owner, mechanic)
}