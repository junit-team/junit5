import aQute.bnd.header.Attrs
import aQute.bnd.header.OSGiHeader
import aQute.bnd.header.Parameters
import aQute.bnd.osgi.Analyzer
import aQute.bnd.osgi.Clazz
import aQute.bnd.osgi.Descriptors.TypeRef
import aQute.bnd.osgi.Instruction
import aQute.bnd.osgi.Instructions
import aQute.bnd.service.AnalyzerPlugin

/*
This is a plugin for bnd which helps analyze the usages of
org.apiguardian.api.API found in the project bytecode. You can read more
about this here: https://bnd.bndtools.org/instructions/export-apiguardian.html

Once the next version of bnd releases (likely 5.0.0) this plugin will be
included in bnd and this class can be removed.

Please ping @rotty3000 to cleanup when that happens.
*/
open class APIGuardianAnnotations : AnalyzerPlugin {

	companion object {
		const val API_ANNOTATION: String = "org/apiguardian/api/API"
		const val INTERNAL_STATUS: String	= "INTERNAL"
		const val STATUS_PROPERTY: String	= "status"
		const val EXPORT_APIGUARDIAN: String = "-export-apiguardian"
		const val MANDATORY_DIRECTIVE: String = "mandatory:"
		const val NO_IMPORT_DIRECTIVE: String = "-noimport:"
	}

	internal enum class Status {
		INTERNAL,
		DEPRECATED,
		EXPERIMENTAL,
		MAINTAINED,
		STABLE
	}

	@Throws(Exception::class)
	override fun analyzeJar(analyzer:Analyzer):Boolean {
		// Opt-in is required.
		val header = OSGiHeader.parseHeader(analyzer.getProperty(EXPORT_APIGUARDIAN))
		if (header.isEmpty()) return false
		val exportPackages = analyzer.getExportPackage()
		val instructions = Instructions(header)
		val apiGuardianPackages = Parameters(false)
		for ((_, c:Clazz) in analyzer.getClassspace()) {
			if (c.isModule() || c.isInnerClass() || c.isSynthetic()) continue
			for ((k:Instruction, v:Attrs) in instructions) {
				if (k.matches(c.getFQN())) {
					if (k.isNegated()) break

					c.annotations(API_ANNOTATION)
						.map({ ann-> Status.valueOf(ann.get(STATUS_PROPERTY)) })
						.max(Status::compareTo)
						.ifPresent({ status->
							val attrs = apiGuardianPackages.computeIfAbsent(
								c.getClassName().getPackageRef().getFQN(), { _->
									Attrs(v)
								}
							)

							attrs.compute(
								STATUS_PROPERTY, { _, v->
									if ((v == null))
										status.name
									else
										if ((Status.valueOf(v).compareTo(status) > 0))
											v
										else
											status.name
								}
							)
						}
					)
				}
			}
		}

		apiGuardianPackages.values.stream()
			.filter({ a-> Status.valueOf(a.get(STATUS_PROPERTY)) === Status.INTERNAL })
			.forEach({ a->
				a.put(MANDATORY_DIRECTIVE, STATUS_PROPERTY)
				a.put(NO_IMPORT_DIRECTIVE, "true")
			})
		exportPackages.mergeWith(apiGuardianPackages, false)
		analyzer.setExportPackage(exportPackages.toString())
		return false
	}
}
