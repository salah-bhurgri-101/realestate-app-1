const AddPropertyPage = () => {
  return (
    <div className="max-w-7xl mx-auto p-8">
      <h2 className="text-2xl font-bold mb-4">Add New Property</h2>
      <form className="bg-white p-6 rounded shadow space-y-4">
        <input className="border p-2 w-full rounded" placeholder="Title" />
        <textarea className="border p-2 w-full rounded" placeholder="Description" />
        <input className="border p-2 w-full rounded" placeholder="Price" />
        <input className="border p-2 w-full rounded" placeholder="Location" />
        <button className="px-4 py-2 bg-indigo-600 text-white rounded">Create (demo)</button>
      </form>
    </div>
  );
};

export default AddPropertyPage;
