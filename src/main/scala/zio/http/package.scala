package zio

import zio.stacktracer.TracingImplicits.disableAutoTrace // scalafix:ok;

package object http extends PathSyntax with RouteDecoderModule {
  
}
