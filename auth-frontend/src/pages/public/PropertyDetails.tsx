import properties from "../../properties";
import { useParams } from "react-router-dom";

const PropertyDetails = () => {
  const { id } = useParams();
  const property = properties.find((p) => p.id === id);

  if (!property) {
    return (
      <div className="max-w-4xl mx-auto p-8">
        <h2 className="text-xl font-bold">Property not found</h2>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-8">
      <div className="bg-white rounded shadow overflow-hidden">
        <div className="h-64 bg-gray-100 flex items-center justify-center">
          <img src={property.image} alt={property.title} className="w-full h-64 object-cover" />
        </div>

        <div className="p-6">
          <h1 className="text-2xl font-bold mb-2">{property.title}</h1>
          <div className="text-indigo-600 font-bold text-lg mb-2">${property.price.toLocaleString()}</div>
          <div className="text-sm text-gray-500 mb-4">{property.location} • {property.bedrooms} beds</div>

          <p className="text-gray-700 mb-4">{property.description}</p>

          <button className="px-4 py-2 bg-indigo-600 text-white rounded">Contact Seller</button>
        </div>
      </div>
    </div>
  );
};

export default PropertyDetails;
