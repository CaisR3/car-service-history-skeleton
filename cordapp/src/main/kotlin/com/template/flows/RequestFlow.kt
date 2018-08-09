package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.ServiceContract
import com.template.ServiceState
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import javax.annotation.Signed

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
object RequestFlow {
    class Initiator(val serviceProvider: Party) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            // Flow implementation goes here
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val builder = TransactionBuilder(notary)

            //For the purposes of this, we're just going to create our service record for the first time here
            // Normally this would be pulled from our vault and would have been issued by manufacturer
            val requestState = ServiceState(ourIdentity, serviceProvider, "KR60 LWT", "")

            builder.addOutputState(requestState, ServiceContract.ID)

            val ptx = serviceHub.signInitialTransaction(builder)

            val session = initiateFlow(serviceProvider)

            val stx = subFlow(CollectSignaturesFlow(ptx, listOf(session)))

            return subFlow(FinalityFlow(stx))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {

                }

            }

            return subFlow(signTransactionFlow)
        }
    }
}