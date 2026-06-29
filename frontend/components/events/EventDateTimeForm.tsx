"use client";

type Props = {
  label: string;
  value: string | null;
  onChange: (value: string) => void;
};

const EventDateTimeForm: React.FC<Props> = ({ label, value, onChange }) => {
  return (
    <div className="mb-2">
      <label>{label}</label>
      <input
        type="datetime-local"
        value={value ?? ""}
        onChange={(e) => onChange(e.target.value)}
        className="block w-full border p-2 rounded"
      />
    </div>
  );
};

export default EventDateTimeForm;