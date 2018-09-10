package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.ServiceContract
import com.template.ServiceState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class RequestFlow(val mechanic: Party, val registration: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // For the purposes of this, we're just going to create our service record for the first time here.
        // Normally this would be pulled from our vault and would have been issued by manufacturer.
        val requestState = null


    }
}