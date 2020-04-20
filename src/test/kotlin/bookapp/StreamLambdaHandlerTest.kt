package bookapp

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.ws.rs.HttpMethod
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class StreamLambdaHandlerTest {

    private var handler: StreamLambdaHandler? = null
    private var lambdaContext: Context? = null


    @Before
    fun setUp() {
        handler = StreamLambdaHandler()
        lambdaContext = MockLambdaContext()
    }

    @Test
    fun ping_streamRequest_respondsWithHello() {
        val requestStream = AwsProxyRequestBuilder("/ping", HttpMethod.GET)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .buildStream()
        val responseStream = ByteArrayOutputStream()
        handle(requestStream, responseStream)
        val response = readResponse(responseStream)
        Assert.assertNotNull(response)
        Assert.assertEquals(Response.Status.OK.statusCode.toLong(), response!!.statusCode.toLong())
        Assert.assertFalse(response.isBase64Encoded)
        Assert.assertTrue(response.body.contains("pong"))
        Assert.assertTrue(response.body.contains("Hello, World!"))
        Assert.assertTrue(response.multiValueHeaders.containsKey(HttpHeaders.CONTENT_TYPE))
        Assert.assertTrue(response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.APPLICATION_JSON))
    }

    @Test
    fun invalidResource_streamRequest_responds404() {
        val requestStream = AwsProxyRequestBuilder("/pong", HttpMethod.GET)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .buildStream()
        val responseStream = ByteArrayOutputStream()
        handle(requestStream, responseStream)
        val response = readResponse(responseStream)
        Assert.assertNotNull(response)
        Assert.assertEquals(Response.Status.NOT_FOUND.statusCode.toLong(), response!!.statusCode.toLong())
    }

    private fun handle(`is`: InputStream, os: ByteArrayOutputStream) {
        try {
            handler!!.handleRequest(`is`, os, lambdaContext!!)
        } catch (e: IOException) {
            e.printStackTrace()
            Assert.fail(e.message)
        }
    }

    private fun readResponse(responseStream: ByteArrayOutputStream): AwsProxyResponse? {
        try {
            return LambdaContainerHandler.getObjectMapper().readValue(responseStream.toByteArray(), AwsProxyResponse::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            Assert.fail("Error while parsing response: " + e.message)
        }
        return null
    }
}