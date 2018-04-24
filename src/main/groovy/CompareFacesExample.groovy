import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.List

import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.model.S3Object
import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.util.IOUtils
import com.amazonaws.services.rekognition.model.BoundingBox
import com.amazonaws.services.rekognition.model.CompareFacesMatch
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.CompareFacesResult
import com.amazonaws.services.rekognition.model.ComparedFace


class CompareFacesExample {

   static void main(String[] args) throws Exception{
       
      String bucketName = "training.makingdevs.com"
      Float similarityThreshold = 70F
      S3Object sourceImage = new S3Object().withBucket(bucketName).withName("videos/image1_superchida.jpg")
      S3Object targetImage = new S3Object().withBucket(bucketName).withName("videos/image4_superchida.jpg")

      AWSCredentials credentials
      try {
           credentials = new ProfileCredentialsProvider("adminuser").getCredentials()
      } catch (Exception e) {
          throw new AmazonClientException("""Cannot load the credentials from the credential profiles file.
          Please make sure that your credentials file is at the correct 
          location (/Users/userid/.aws/credentials), and is in valid format.""", e)
      }
      
      AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
               .standard()
               .withRegion(Regions.US_EAST_1)
               .withCredentials(new AWSStaticCredentialsProvider(credentials))
               .build()
       //Load source and target images and create input parameters

       Image source =new Image().withS3Object(sourceImage)
       Image target =new Image().withS3Object(targetImage)

      
       CompareFacesRequest request = new CompareFacesRequest()
               .withSourceImage(source)
               .withTargetImage(target)
               .withSimilarityThreshold(similarityThreshold)

       // Call operation
       CompareFacesResult compareFacesResult=rekognitionClient.compareFaces(request)


       // Display results
      List <CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches()
      for (CompareFacesMatch match: faceDetails){
          ComparedFace face = match.getFace()
          BoundingBox position = face.getBoundingBox()
          println """Face at ${position.getLeft().toString()}
          ${position.getTop()}
          Matches with  ${face.getConfidence().toString()} % confidence."""
      }

       List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces()

      println """There were ${uncompared.size()} that did not match
      Source image rotation: ${compareFacesResult.getSourceImageOrientationCorrection()}
      Target image rotation: ${compareFacesResult.getTargetImageOrientationCorrection()}"""
   }
}