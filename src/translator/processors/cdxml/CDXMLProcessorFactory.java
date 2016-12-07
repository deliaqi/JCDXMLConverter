package translator.processors.cdxml;

import translator.ParseElementDefinition;
import translator.processors.ProcessorException;
import translator.processors.ProcessorFactory;

public class CDXMLProcessorFactory extends ProcessorFactory {
    
    public CDXMLProcessorFactory() {
        try {
            this.addProcessor(ParseElementDefinition.SOLID_BOND, new SolidBondProcessor());
            this.addProcessor(ParseElementDefinition.WEDGE_HASH_BEGIN_BOND, new WedgeHashBeginBondProcessor());
            this.addProcessor(ParseElementDefinition.WEDGE_HASH_END_BOND, new WedgeHashEndBondProcessor());
            this.addProcessor(ParseElementDefinition.HOLLOW_WEDGE_BOND, new HollowBondProcessor());
            this.addProcessor(ParseElementDefinition.HASH_BOND, new HashBondProcessor());
            this.addProcessor(ParseElementDefinition.DASH_BOND, new DashBondProcessor());
            this.addProcessor(ParseElementDefinition.DATIVE_BOND, new DativeBondProcessor());
            this.addProcessor(ParseElementDefinition.WAVY_BOND, new WavyBondProcessor());
            this.addProcessor(ParseElementDefinition.WAVY_2_BOND, new DoubleEitherBondProcessor());
            this.addProcessor(ParseElementDefinition.TEXT, new TextProcessor());
            this.addProcessor(ParseElementDefinition.R_LOGIC, new TextProcessor());
            this.addProcessor(ParseElementDefinition.TLC_PLATE, new TLCPlateProcessor());
            this.addProcessor(ParseElementDefinition.DOUBLE_EITHER_BOND, new DoubleEitherBondProcessor());
            this.addProcessor(ParseElementDefinition.SPLINE, new SplineProcessor());
            this.addProcessor(ParseElementDefinition.ARROW, new ArrowProcessor());
            this.addProcessor(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE, new RectangleProcessor());
            this.addProcessor(ParseElementDefinition.GRAPHIC_ROUNDED_RECTANGLE, new RoundedRectangleProcess());
            this.addProcessor(ParseElementDefinition.GRAPHIC_CIRCLE, new CircleProcessor());
            this.addProcessor(ParseElementDefinition.GRAPHIC_OVAL, new OvalProcessor());
            this.addProcessor(ParseElementDefinition.ARROW_HEADLESS, new ArcProcessor());
            this.addProcessor(ParseElementDefinition.ARROW_HOLLOW, new HollowArrowProcessor());
            this.addProcessor(ParseElementDefinition.GRAPHIC_TYPE_BRACKET, new BracketsProcessor());
            this.addProcessor(ParseElementDefinition.ARROW_WAVY, new WavyLineProcessor());
            this.addProcessor(ParseElementDefinition.TABLE, new TableProcessor());
            this.addProcessor(ParseElementDefinition.SYMBOL_TYPE_RADICAL, new RadicalProcessor());
            this.addProcessor(ParseElementDefinition.SYMBOL_TYPE_CHARGE, new ChargeProcessor());
            this.addProcessor(ParseElementDefinition.SYMBOL_TYPE_DAGGERS, new DaggerProcessor());
            this.addProcessor(ParseElementDefinition.NODE, new NodeProcessor());
            this.addProcessor(ParseElementDefinition.ORBITAL_LOBE, new OrbitalProcessor());
            this.addProcessor(ParseElementDefinition.ORBITAL_P, new POrbitalProcessor());
            this.addProcessor(ParseElementDefinition.ORBITAL_HYBRID, new HybridOrbitalProcessor());
            this.addProcessor(ParseElementDefinition.ORBITAL_DZ2, new DZ2OrbitalProcessor());
            this.addProcessor(ParseElementDefinition.ORBITAL_DXY, new DXYOrbitalProcessor());
            this.addProcessor(ParseElementDefinition.EMBEDDED_OBJECT, new EmbeddedObjectProcessor());
            this.addProcessor(ParseElementDefinition.STOICHIOMETRY_GRID, new StoichiometryGridProcessor());
            this.addProcessor(ParseElementDefinition.SPECTRUM, new SpectrumProcessor());
            this.addProcessor(ParseElementDefinition.ALTERNATIVE_GROUP, new AlternativeGroupProcessor());
            this.addProcessor(ParseElementDefinition.GRAPHIC_TYPE_SYMBOL, new QuerySymbolsProcessor());
            this.addProcessor(ParseElementDefinition.CONSTRAINT, new ConstraintProcessor());
            this.addProcessor(ParseElementDefinition.GEOMETRY, new GeometryProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_1_SUBSTRATE_ENZYME, new SubstrateEnzyme1Processor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_2_SUBSTRATE_ENZYME, new SubstrateEnzyme2Processor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_RECEPTOR, new ReceptorProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_ION_CHANNEL, new IonChannelProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_IMMUNOGLOBIN, new InmunoglobinProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_ALPHA, new GProteinAlphaProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_BETA, new GProteinBetaProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_GAMMA, new GProteinGamaProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_GOLGI, new GolgiProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_ENDOPLASMIC_RETICULUM, new EndoplasmicReticulumProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_CLOUD, new CloudProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_RIBOSOME_A, new RibosomeAProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_RIBOSOME_B, new RibosomeBProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_T_RNA, new TRNAProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_MITOCHONDRION, new MitochondrionProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_HELIX_PROTEIN, new HelixProteinProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_DNA, new DNAProcessor());
            this.addProcessor(ParseElementDefinition.BIO_SHAPE_ORNAMENTED_BIO_SHAPE, new OrnamentedShapeProcessor());
 			this.addProcessor(ParseElementDefinition.PLASMID_MAP, new PlasmidMapProcessor());
            this.addProcessor(ParseElementDefinition.PLASMID_REGION, new PlasmidRegionProcessor());
            this.addProcessor(ParseElementDefinition.OBJECT_TAG, new ObjectTagProcessor());
        } catch (ProcessorException ex) {
            ex.printStackTrace();
        }
    }
    
}
