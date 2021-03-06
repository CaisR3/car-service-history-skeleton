package com.template

import net.corda.core.contracts.*
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

    }


    // Used to indicate the transaction's intent.
    interface Commands : CommandData {

    }
}

data class ServiceState(val owner: Party, override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants = listOf(owner)
}